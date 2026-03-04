import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import axios from "axios";
import { loginUser } from "../services/apiServices";
import "./auth.css";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const user = await loginUser({ email, password });

      localStorage.setItem("user", JSON.stringify(user));

      if (user.role === "VISITOR") navigate("/visitor-dashboard");
      else if (user.role === "ADMIN") navigate("/admin-dashboard");

    } catch (err) {
      alert("Login failed");
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="header-icon-container">
            <ShieldCheck size={36} color="white" strokeWidth={1.5} />
          </div>
          <h1>KnockKnock</h1>
          <p>Condominium Visitor Management</p>
        </div>

        <div className="auth-content">
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="input-group">
              <label>EMAIL ADDRESS</label>
              <input type="email" placeholder="name@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="input-group">
              <label>PASSWORD</label>
              <input type="password" placeholder="********" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <button type="submit" className="primary-btn">Sign In</button>
          </form>

          <div className="divider"><span>OR CONTINUE WITH</span></div>
          <button type="button" className="google-btn">
            <img src="https://fonts.gstatic.com/s/i/productlogos/googleg/v6/24px.svg" alt="Google" />
            Google Account
          </button>
          
          <p className="signup-text">
            Don’t have an account? <Link to="/register" className="link">Sign Up</Link>
          </p>
        </div>
      </div>
    </div>
  );
}