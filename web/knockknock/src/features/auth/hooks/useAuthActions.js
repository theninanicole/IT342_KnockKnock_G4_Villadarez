import { useCallback, useState } from "react";

// Generic auth hook for email/password forms and optional social login
export const useAuthActions = ({ onSubmit, onGoogleLogin } = {}) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = useCallback(
    async (event) => {
      if (event && typeof event.preventDefault === "function") {
        event.preventDefault();
      }

      if (typeof onSubmit === "function") {
        await onSubmit({ email, password });
      }
    },
    [onSubmit, email, password]
  );

  const handleGoogleLoginClick = useCallback(async () => {
    if (typeof onGoogleLogin === "function") {
      await onGoogleLogin();
    }
  }, [onGoogleLogin]);

  return {
    email,
    setEmail,
    password,
    setPassword,
    handleSubmit,
    handleGoogleLoginClick,
  };
};
