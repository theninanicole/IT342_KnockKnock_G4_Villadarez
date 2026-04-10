import React from "react";
import { Link } from "react-router-dom";
import { useRegisterActions } from "@hooks/useRegisterActions";
import PersonalInformationFields from "@components/auth/PersonalInformationFields";
import CondoInformationFields from "@components/auth/CondoInformationFields";
import AuthInput from "@components/auth/AuthInput";
import AuthButton from "@components/auth/AuthButton";
import AuthDivider from "@components/auth/AuthDivider";
import SocialButton from "@components/auth/SocialButton";
import AuthHeader from "@components/auth/AuthHeader";

export default function Register() {
  const {
    userRole,
    setUserRole,
    formData,
    googleAdminStep,
    handleInputChange,
    handleSubmit,
    onGoogleRegister,
  } = useRegisterActions();


  return (
    <div className="min-h-screen bg-slate-100 flex justify-center items-center px-4 py-10">
      <div className="w-full max-w-xl bg-white rounded-2xl shadow-[0_20px_40px_rgba(15,23,42,0.18)] overflow-hidden">
        <AuthHeader subtitle="Create your account to get started" />

        <div className="px-8 py-8">
          {/* Role selector */}
          <div className="mb-7 flex gap-2 rounded-xl bg-slate-100 p-1">
            <button
              type="button"
              className={`flex-1 rounded-lg py-3 text-sm font-semibold transition-all duration-150 focus:outline-none ${
                userRole === "visitor"
                  ? "bg-white text-[#2d6df6] shadow-sm"
                  : "text-slate-500 hover:text-[#2d6df6]"
              }`}
              onClick={() => setUserRole("visitor")}
            >
              Visitor
            </button>
            <button
              type="button"
              className={`flex-1 rounded-lg py-3 text-sm font-semibold transition-all duration-150 focus:outline-none ${
                userRole === "admin"
                  ? "bg-white text-[#2d6df6] shadow-sm"
                  : "text-slate-500 hover:text-[#2d6df6]"
              }`}
              onClick={() => setUserRole("admin")}
            >
              Administrator
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {(userRole === "visitor" || (userRole === "admin" && !googleAdminStep)) && (
              <PersonalInformationFields formData={formData} onChange={handleInputChange} />
            )}

            {userRole === "admin" && (
              <CondoInformationFields
                formData={formData}
                onChange={handleInputChange}
                googleAdminStep={googleAdminStep}
              />
            )}

            {(userRole === "visitor" || (userRole === "admin" && !googleAdminStep)) && (
              <div className="grid gap-4 md:grid-cols-2">
                <AuthInput
                  label="PASSWORD"
                  type="password"
                  name="password"
                  placeholder="********"
                  value={formData.password}
                  onChange={handleInputChange}
                />
                <AuthInput
                  label="CONFIRM PASSWORD"
                  type="password"
                  name="confirmPassword"
                  placeholder="********"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                />
              </div>
            )}
            <AuthButton
              label={
                userRole === "admin"
                  ? googleAdminStep
                    ? "Complete Registration"
                    : "Create Account"
                  : "Create Account"
              }
            />
          </form>

          <AuthDivider />

          <SocialButton onClick={onGoogleRegister} />

          <p className="text-center text-sm text-slate-500">
            Already have an account? {" "}
            <Link to="/" className="font-semibold text-[#2d6df6]">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
