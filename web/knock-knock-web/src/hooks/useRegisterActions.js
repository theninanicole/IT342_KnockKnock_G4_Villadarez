import { useCallback, useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "@store/AuthContext";
import {
  handleGoogleRegister,
  handleRegisterSubmit,
  requestGoogleIdToken,
} from "@services/authService";
import { registerAdminWithGoogle } from "@api/apiServices";

export const useRegisterActions = () => {
  const [userRole, setUserRole] = useState("visitor");
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    contactNumber: "",
    password: "",
    confirmPassword: "",
    condoName: "",
    condoAddress: "",
    condoContact: "",
  });
  const [googleIdToken, setGoogleIdToken] = useState(null);
  const [googleAdminStep, setGoogleAdminStep] = useState(false);

  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleInputChange = useCallback((event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  }, []);

  const handleSubmit = useCallback(
    async (event) => {
      if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
      }

      if (userRole === "admin" && googleAdminStep && googleIdToken) {
        try {
          const data = await registerAdminWithGoogle({
            idToken: googleIdToken,
            condoName: formData.condoName,
            condoAddress: formData.condoAddress,
            condoContact: formData.condoContact || formData.contactNumber || null,
          });

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
          navigate("/admin-dashboard");
        } catch (err) {
          const errorMessage =
            err?.response?.data?.error?.message ||
            err?.response?.data?.error ||
            err?.message ||
            "Google registration failed";
          // eslint-disable-next-line no-alert
          alert(errorMessage);
        }
        return;
      }

      handleRegisterSubmit({ userRole, formData, login, navigate });
    },
    [userRole, googleAdminStep, googleIdToken, formData, login, navigate]
  );

  const onGoogleRegister = useCallback(
    async () => {
      if (userRole === "visitor") {
        await handleGoogleRegister({ userRole, formData, login, navigate });
        return;
      }

      try {
        const token = await requestGoogleIdToken();
        setGoogleIdToken(token);
        setGoogleAdminStep(true);
      } catch (err) {
        // eslint-disable-next-line no-alert
        alert(err.message || "Google sign-in failed");
      }
    },
    [userRole, formData, login, navigate]
  );

  return {
    userRole,
    setUserRole,
    formData,
    googleAdminStep,
    handleInputChange,
    handleSubmit,
    onGoogleRegister,
  };
};
