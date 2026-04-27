import { toast } from "react-toastify";
import {
  loginUser,
  registerVisitor,
  registerAdmin,
  loginWithGoogle,
  registerVisitorWithGoogle,
  registerAdminWithGoogle,
} from "@api/apiServices";

const GOOGLE_SCRIPT_ID = "google-identity-services";
const GOOGLE_PROXY_BUTTON_ID = "google-hidden-proxy-button";

const loadGoogleScript = () =>
  new Promise((resolve, reject) => {
    if (window.google?.accounts?.id) {
      resolve();
      return;
    }

    const existingScript = document.getElementById(GOOGLE_SCRIPT_ID);
    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(), { once: true });
      existingScript.addEventListener("error", () => reject(new Error("Failed to load Google script")), {
        once: true,
      });
      return;
    }

    const script = document.createElement("script");
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;
    script.id = GOOGLE_SCRIPT_ID;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Failed to load Google script"));
    document.head.appendChild(script);
  });

export const requestGoogleIdToken = async () => {
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
  if (!clientId) {
    throw new Error("Google Client ID is not configured");
  }

  await loadGoogleScript();

  return new Promise((resolve, reject) => {
    let settled = false;
    let timeoutId;

    window.google.accounts.id.initialize({
      client_id: clientId,
      use_fedcm_for_prompt: true,
      use_fedcm_for_button: true,
      callback: (response) => {
        if (settled) return;
        settled = true;
        if (timeoutId) {
          clearTimeout(timeoutId);
        }
        if (response?.credential) {
          resolve(response.credential);
        } else {
          reject(new Error("Google sign-in failed"));
        }
      },
    });

    let container = document.getElementById(GOOGLE_PROXY_BUTTON_ID);
    if (!container) {
      container = document.createElement("div");
      container.id = GOOGLE_PROXY_BUTTON_ID;
      container.style.position = "fixed";
      container.style.left = "-9999px";
      container.style.top = "-9999px";
      container.style.opacity = "0";
      document.body.appendChild(container);
    }

    container.innerHTML = "";

    window.google.accounts.id.renderButton(container, {
      type: "standard",
      theme: "outline",
      size: "large",
      text: "continue_with",
      width: 260,
    });

    const actualGoogleButton = container.querySelector('div[role="button"]');
    if (!actualGoogleButton) {
      settled = true;
      reject(new Error("Google sign-in is unavailable"));
      return;
    }

    timeoutId = setTimeout(() => {
      if (settled) return;
      settled = true;
      reject(new Error("Google sign-in did not return a credential. Please allow popups and try again."));
    }, 60000);

    actualGoogleButton.dispatchEvent(
      new MouseEvent("click", {
        view: window,
        bubbles: true,
        cancelable: true,
      })
    );
  });
};

// Handles login flow: API call, persisting user/token, context update, and navigation
export const handleLoginSubmit = async ({ email, password, login, navigate }) => {
  try {
    const data = await loginUser({ email, password });
    const apiUser = data.user || data;
    const user = {
      id: apiUser.id,
      email: apiUser.email,
      fullName: apiUser.fullName,
      role: apiUser.role,
      condo: apiUser.condo || null,
    };
    const jwtToken = data.token || localStorage.getItem("token");
    if (data.token) {
      localStorage.setItem("token", data.token);
    }
    await login(user, jwtToken);
    if (user.role === "VISITOR") {
      navigate("/visitor-dashboard");
    } else if (user.role === "CONDOMINIUM_ADMIN") {
      navigate("/admin-dashboard");
    } else {
      navigate("/");
    }
  } catch (err) {
    const msg = err?.response?.data?.error?.toLowerCase() || "";
    if (msg.includes("invalid") || msg.includes("credentials")) {
      toast.error("Incorrect email or password.");
    } else {
      toast.error("Unable to log in. Please try again.");
    }
  }
};

// Handles registration for both visitor and admin, plus auto-login + navigation
export const handleRegisterSubmit = async ({ userRole, formData, login, navigate }) => {
  try {
    let data;
    if (userRole === "visitor") {
      data = await registerVisitor(formData);
    } else {
      data = await registerAdmin(formData);
    }
    const apiUser = data.user || data;
    const jwtToken = data.token;
    const user = {
      id: apiUser.id,
      email: apiUser.email,
      fullName: apiUser.fullName,
      role: apiUser.role,
      condo: apiUser.condo || null,
    };
    if (data.token) {
      localStorage.setItem("token", data.token);
    }
    await login(user, jwtToken);
    if (user.role === "VISITOR") {
      navigate("/visitor-dashboard");
    } else if (user.role === "CONDOMINIUM_ADMIN") {
      navigate("/admin-dashboard");
    } else {
      navigate("/");
    }
  } catch (err) {
    const msg = err?.response?.data?.error?.toLowerCase() || "";
    if (msg.includes("email") && msg.includes("exists")) {
      toast.error("This email is already registered.");
    } else {
      toast.error("Unable to register. Please try again.");
    }
  }
};

export const handleGoogleLogin = async ({ login, navigate }) => {
  try {
    const idToken = await requestGoogleIdToken();
    const data = await loginWithGoogle(idToken);
    const apiUser = data.user || data;
    const jwtToken = data.token;

    const user = {
      id: apiUser.id,
      email: apiUser.email,
      fullName: apiUser.fullName,
      role: apiUser.role,
      condo: apiUser.condo || null,
    };

    if (data.token) {
      localStorage.setItem("token", data.token);
    }

    // Pass user AND token to AuthContext so Supabase session is set up
    await login(user, jwtToken);

    if (user.role === "VISITOR") {
      navigate("/visitor-dashboard");
    } else if (user.role === "CONDOMINIUM_ADMIN") {
      navigate("/admin-dashboard");
    } else {
      navigate("/");
    }
  } catch (err) {
    const msg = err?.response?.data?.error?.toLowerCase() || "";
    if (msg.includes("email") && msg.includes("exists")) {
      toast.error("This email is already registered.");
    } else if (msg.includes("google")) {
      toast.error("Google login failed. Please try again.");
    } else {
      toast.error("Unable to log in with Google. Please try again.");
    }
  }
};

export const handleGoogleRegister = async ({ userRole, formData, login, navigate }) => {
  try {
    const idToken = await requestGoogleIdToken();
    let data;

    if (userRole === "visitor") {
      data = await registerVisitorWithGoogle(idToken);
    } else {
      data = await registerAdminWithGoogle({
        idToken,
        condoName: formData.condoName,
        condoAddress: formData.condoAddress,
        condoContact: formData.condoContact || formData.contactNumber || null,
      });
    }

    const apiUser = data.user || data;
    const jwtToken = data.token;
    const user = {
      id: apiUser.id,
      email: apiUser.email,
      fullName: apiUser.fullName,
      role: apiUser.role,
      condo: apiUser.condo || null,
    };

    if (data.token) {
      localStorage.setItem("token", data.token);
    }

    // Pass user AND token to AuthContext so Supabase session is set up
    await login(user, jwtToken);

    if (user.role === "VISITOR") {
      navigate("/visitor-dashboard");
    } else if (user.role === "CONDOMINIUM_ADMIN") {
      navigate("/admin-dashboard");
    } else {
      navigate("/");
    }
  } catch (err) {
    const errorMessage =
      err?.response?.data?.error?.message ||
      err?.response?.data?.error ||
      err?.message ||
      "Google registration failed";
    alert(errorMessage);
  }
};
