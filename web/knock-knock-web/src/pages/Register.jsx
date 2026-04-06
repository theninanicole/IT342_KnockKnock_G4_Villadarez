import React, { useState, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import { AuthContext } from "../context/AuthContext";
import { handleGoogleRegister, handleRegisterSubmit, requestGoogleIdToken } from "../services/authHandlers";
import { registerAdminWithGoogle } from "../services/apiServices";

export default function Register() {
  const [userRole, setUserRole] = useState("visitor"); 
  const [formData, setFormData] = useState({
    fullName: "", email: "", contactNumber: "", password: "", confirmPassword: "",
    condoName: "", condoAddress: "", condoContact: ""
  });
  const [googleIdToken, setGoogleIdToken] = useState(null);
  const [googleAdminStep, setGoogleAdminStep] = useState(false);
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Admin Google flow: second step, use stored Google token + condo details
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
        alert(errorMessage);
      }
      return;
    }

    // Default email/password flow for visitor and admin
    handleRegisterSubmit({ userRole, formData, login, navigate });
  };

  const onGoogleRegister = async () => {
    // Visitor keeps the original one-step Google register
    if (userRole === "visitor") {
      await handleGoogleRegister({ userRole, formData, login, navigate });
      return;
    }

    // Admin: first step – obtain Google ID token only, then show condo details
    try {
      const token = await requestGoogleIdToken();
      setGoogleIdToken(token);
      setGoogleAdminStep(true);
    } catch (err) {
      alert(err.message || "Google sign-in failed");
    }
  };


  return (
    <div className="min-h-screen bg-slate-100 flex justify-center items-center px-4 py-10">
      <div className="w-full max-w-xl bg-white rounded-2xl shadow-[0_20px_40px_rgba(15,23,42,0.18)] overflow-hidden">
        {/* Header Section */}
        <div className="bg-[#2d6df6] text-white text-center px-8 pt-12 pb-10">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/20">
            <ShieldCheck size={32} color="white" strokeWidth={1.5} />
          </div>
          <h1 className="text-3xl font-bold tracking-tight">KnockKnock</h1>
          <p className="mt-2 text-sm opacity-90">Create your account to get started</p>
        </div>

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
              <div>
                <div className="mb-4 mt-1 border-b border-slate-100 pb-1 text-[11px] font-extrabold tracking-[0.12em] text-[#2d6df6]">
                  PERSONAL INFORMATION
                </div>

                <div className="mb-4 space-y-2">
                  <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                    FULL NAME
                  </label>
                  <input
                    type="text"
                    name="fullName"
                    placeholder="Juan Dela Cruz"
                    onChange={handleInputChange}
                    required
                    className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
                  />
                </div>

                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                      EMAIL ADDRESS
                    </label>
                    <input
                      type="email"
                      name="email"
                      placeholder="name@email.com"
                      onChange={handleInputChange}
                      required
                      className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                      CONTACT NUMBER
                    </label>
                    <input
                      type="text"
                      name="contactNumber"
                      placeholder="09123456789"
                      onChange={handleInputChange}
                      required
                      className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
                    />
                  </div>
                </div>
              </div>
            )}

            {userRole === "admin" && (
              <div className="space-y-4">
                <div className="mt-2 border-b border-slate-100 pb-1 text-[11px] font-extrabold tracking-[0.12em] text-[#2d6df6]">
                  CONDOMINIUM DETAILS
                </div>
                {googleAdminStep && (
                  <p className="text-xs font-medium text-slate-500">
                    Step 2: Google sign-in successful. Please enter your condominium details to complete registration.
                  </p>
                )}
                <div className="space-y-2">
                  <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                    CONDOMINIUM NAME
                  </label>
                  <input
                    type="text"
                    name="condoName"
                    placeholder="e.g. Makati Tower"
                    onChange={handleInputChange}
                    required
                    className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
                  />
                </div>
                <div className="space-y-2">
                  <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                    CONDOMINIUM ADDRESS
                  </label>
                  <input
                    type="text"
                    name="condoAddress"
                    placeholder="123 Ayala Avenue..."
                    onChange={handleInputChange}
                    required
                    className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
                  />
                </div>
              </div>
            )}

            {(userRole === "visitor" || (userRole === "admin" && !googleAdminStep)) && (
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                    PASSWORD
                  </label>
                  <input
                    type="password"
                    name="password"
                    placeholder="********"
                    onChange={handleInputChange}
                    required
                    className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
                  />
                </div>
                <div className="space-y-2">
                  <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                    CONFIRM PASSWORD
                  </label>
                  <input
                    type="password"
                    name="confirmPassword"
                    placeholder="********"
                    onChange={handleInputChange}
                    required
                    className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
                  />
                </div>
              </div>
            )}

            <button
              type="submit"
              className="mt-2 w-full rounded-xl bg-[#2d6df6] py-4 text-[16px] font-semibold text-white shadow-sm transition hover:bg-[#1e56d1]"
            >
              {userRole === "admin"
                ? googleAdminStep
                  ? "Complete Registration"
                  : "Create Account"
                : "Create Account"}
            </button>
          </form>

          <div className="mt-8 mb-6 flex items-center text-[11px] font-bold tracking-[0.12em] text-slate-400">
            <span className="h-px flex-1 bg-slate-200" />
            <span className="px-4">OR CONTINUE WITH</span>
            <span className="h-px flex-1 bg-slate-200" />
          </div>

          <button
            type="button"
            onClick={onGoogleRegister}
            className="mb-6 flex w-full items-center justify-center gap-3 rounded-xl border border-slate-200 bg-white py-3.5 text-[15px] font-semibold text-slate-900 transition hover:bg-slate-50"
          >
            <img
              src="https://fonts.gstatic.com/s/i/productlogos/googleg/v6/24px.svg"
              alt="Google"
              className="h-5 w-5"
            />
            Google Account
          </button>

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
