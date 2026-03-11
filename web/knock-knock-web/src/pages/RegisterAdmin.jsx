import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import axios from "axios";
import "./auth.css";

export default function RegisterAdmin() {
  const [formData, setFormData] = useState({ 
    fullName: "", email: "", contactNumber: "", password: "", confirmPassword: "",
    condoName: "", condoAddress: "", condoContact: "" 
  });
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post("http://localhost:8080/api/auth/register/condo-admin", formData); //
      navigate("/"); //
    } catch (err) {
      alert(err.response?.data?.error?.message || "Admin registration failed");
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card admin-card">
        <div className="auth-header">
          <div className="header-icon-container"><ShieldCheck size={36} color="white" strokeWidth={1.5} /></div>
          <h1>KnockKnock</h1>
          <p>Register Condominium</p>
        </div>

        <div className="auth-content">
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="section-label">PERSONAL DETAILS</div>
            <div className="input-group">
              <label>FULL NAME</label>
              <input type="text" onChange={(e) => setFormData({...formData, fullName: e.target.value})} required />
            </div>
            <div className="grid-2">
              <div className="input-group"><label>EMAIL</label>
                <input type="email" onChange={(e) => setFormData({...formData, email: e.target.value})} required />
              </div>
              <div className="input-group"><label>CONTACT</label>
                <input type="text" onChange={(e) => setFormData({...formData, contactNumber: e.target.value})} required />
              </div>
            </div>

            <div className="section-label">CONDOMINIUM INFORMATION</div>
            <div className="input-group"><label>CONDO NAME</label>
              <input type="text" onChange={(e) => setFormData({...formData, condoName: e.target.value})} required />
            </div>
            <div className="input-group"><label>ADDRESS</label>
              <input type="text" onChange={(e) => setFormData({...formData, condoAddress: e.target.value})} required />
            </div>
            
            <div className="grid-2">
              <div className="input-group"><label>PASSWORD</label>
                <input type="password" onChange={(e) => setFormData({...formData, password: e.target.value})} required />
              </div>
              <div className="input-group"><label>CONFIRM</label>
                <input type="password" onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})} required />
              </div>
            </div>
            <button type="submit" className="primary-btn">Register Condominium</button>
          </form>
          <p className="signup-text">Already have an account? <Link to="/" className="link">Sign In</Link></p>
        </div>
      </div>
    </div>
  );
}