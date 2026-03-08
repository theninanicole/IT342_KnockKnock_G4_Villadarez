import { loginUser, registerVisitor, registerAdmin } from "./apiServices";

// Handles login flow: API call, persisting user/token, context update, and navigation
export const handleLoginSubmit = async ({ email, password, login, navigate }) => {
  try {
    const data = await loginUser({ email, password });

    const user = {
      id: data.id,
      email: data.email,
      fullName: data.fullName,
      role: data.role,
    };

    if (data.token) {
      localStorage.setItem("token", data.token);
    }

    // Let AuthContext handle persisting user state
    login(user);

    if (user.role === "VISITOR") {
      navigate("/visitor-dashboard");
    } else if (user.role === "CONDOMINIUM_ADMIN") {
      navigate("/admin-dashboard");
    } else {
      navigate("/");
    }
  } catch (err) {
    alert("Login failed");
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

    const user = {
      id: apiUser.id,
      email: apiUser.email,
      fullName: apiUser.fullName,
      role: apiUser.role,
    };

    if (data.token) {
      localStorage.setItem("token", data.token);
    }

    login(user);

    if (user.role === "VISITOR") {
      navigate("/visitor-dashboard");
    } else if (user.role === "CONDOMINIUM_ADMIN") {
      navigate("/admin-dashboard");
    } else {
      navigate("/");
    }
  } catch (err) {
    alert("Registration failed");
  }
};
