import React, { useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthContext } from "@store/AuthContext";
import { handleGoogleLogin, handleLoginSubmit } from "@services/authService";
import { useAuthActions } from "@hooks/useAuthActions";
import AuthInput from "@components/auth/AuthInput";
import AuthButton from "@components/auth/AuthButton";
import AuthDivider from "@components/auth/AuthDivider";
import SocialButton from "@components/auth/SocialButton";
import AuthHeader from "@components/auth/AuthHeader";

export default function Login() {
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const {
    email,
    setEmail,
    password,
    setPassword,
    handleSubmit,
    handleGoogleLoginClick,
  } = useAuthActions({
    onSubmit: ({ email, password }) =>
      handleLoginSubmit({ email, password, login, navigate }),
    onGoogleLogin: () => handleGoogleLogin({ login, navigate }),
  });

  return (
    <div className="min-h-screen bg-slate-100 flex justify-center items-center px-4 py-10">
      <div className="w-full max-w-xl bg-white rounded-2xl shadow-[0_20px_40px_rgba(15,23,42,0.18)] overflow-hidden">
        <AuthHeader subtitle="Condominium Visitor Management" />

        <div className="px-8 py-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            <AuthInput
              label="EMAIL ADDRESS"
              type="email"
              placeholder="name@gmail.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
            <AuthInput
              label="PASSWORD"
              type="password"
              placeholder="********"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
            <AuthButton label="Sign In" />
          </form>

          <AuthDivider />

          <SocialButton onClick={handleGoogleLoginClick} />

          <p className="text-center text-sm text-slate-500">
            Don’t have an account? {" "}
            <Link to="/register" className="font-semibold text-[#2d6df6]">
              Sign Up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}