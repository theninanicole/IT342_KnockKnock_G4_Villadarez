import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import axios from "axios";
import "./auth.css";

export default function RegisterVisitor() {
  const [formData, setFormData] = useState({ fullName: "", email: "", contactNumber: "", password: "", confirmPassword: "" });
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post("http://localhost:8080/api/auth/register/visitor", formData); //
      navigate("/"); // Redirect to login after success
    } catch (err) {
      alert(err.response?.data?.error?.message || "Registration failed");
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
          <p>Create Visitor Account</p>
        </div>

        <div className="auth-content">
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="input-group">
              <label>FULL NAME</label>
              <input type="text" placeholder="Juan Dela Cruz" onChange={(e) => setFormData({...formData, fullName: e.target.value})} required />
            </div>
            <div className="grid-2">
              <div className="input-group"><label>EMAIL</label>
                <input type="email" placeholder="name@email.com" onChange={(e) => setFormData({...formData, email: e.target.value})} required />
              </div>
              <div className="input-group"><label>CONTACT</label>
                <input type="text" placeholder="0912..." onChange={(e) => setFormData({...formData, contactNumber: e.target.value})} required />
              </div>
            </div>
            <div className="grid-2">
              <div className="input-group"><label>PASSWORD</label>
                <input type="password" placeholder="********" onChange={(e) => setFormData({...formData, password: e.target.value})} required />
              </div>
              <div className="input-group"><label>CONFIRM</label>
                <input type="password" placeholder="********" onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})} required />
              </div>
            </div>
            <button type="submit" className="primary-btn">Create Account</button>
          </form>
          <p className="signup-text">Already have an account? <Link to="/" className="link">Sign In</Link></p>
        </div>
      </div>
    </div>
  );
}