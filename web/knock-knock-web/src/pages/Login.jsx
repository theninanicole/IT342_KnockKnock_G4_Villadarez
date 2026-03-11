import React, { useState, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import { AuthContext } from "../context/AuthContext";
import { handleGoogleLogin, handleLoginSubmit } from "../services/authHandlers";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    handleLoginSubmit({ email, password, login, navigate });
  };

  const onGoogleLogin = async () => {
    await handleGoogleLogin({ login, navigate });
  };

  return (
    <div className="min-h-screen bg-slate-100 flex justify-center items-center px-4 py-10">
      <div className="w-full max-w-xl bg-white rounded-2xl shadow-[0_20px_40px_rgba(15,23,42,0.18)] overflow-hidden">
        <div className="bg-[#2d6df6] text-white text-center px-8 pt-12 pb-10">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/20">
            <ShieldCheck size={32} color="white" strokeWidth={1.5} />
          </div>
          <h1 className="text-3xl font-bold tracking-tight">KnockKnock</h1>
          <p className="mt-2 text-sm opacity-90">Condominium Visitor Management</p>
        </div>

        <div className="px-8 py-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                EMAIL ADDRESS
              </label>
              <input
                type="email"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
              />
            </div>
            <div className="space-y-2">
              <label className="block text-[11px] font-extrabold tracking-[0.08em] text-slate-400">
                PASSWORD
              </label>
              <input
                type="password"
                placeholder="********"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-[15px] text-slate-900 outline-none transition focus:border-[#2d6df6] focus:bg-white focus:ring-4 focus:ring-[#2d6df6]/10"
              />
            </div>
            <button
              type="submit"
              className="mt-2 w-full rounded-xl bg-[#2d6df6] py-4 text-[16px] font-semibold text-white shadow-sm transition hover:bg-[#1e56d1]"
            >
              Sign In
            </button>
          </form>

          <div className="mt-8 mb-6 flex items-center text-[11px] font-bold tracking-[0.12em] text-slate-400">
            <span className="h-px flex-1 bg-slate-200" />
            <span className="px-4">OR CONTINUE WITH</span>
            <span className="h-px flex-1 bg-slate-200" />
          </div>

          <button
            type="button"
            onClick={onGoogleLogin}
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