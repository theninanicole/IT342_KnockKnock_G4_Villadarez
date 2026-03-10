import React, { useState, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import { AuthContext } from "../context/AuthContext";
import { handleRegisterSubmit } from "../services/authHandlers";
import "./auth.css";

export default function Register() {
  const [userRole, setUserRole] = useState("visitor"); 
  const [formData, setFormData] = useState({
    fullName: "", email: "", contactNumber: "", password: "", confirmPassword: "",
    condoName: "", condoAddress: "", condoContact: ""
  });
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
        e.preventDefault();
        handleRegisterSubmit({ userRole, formData, login, navigate });
    };


  return (
    <div className="auth-container">
      <div className="auth-card">
        {/* Header Section */}
        <div className="auth-header">
          <div className="header-icon-container">
            <ShieldCheck size={36} color="white" strokeWidth={1.5} />
          </div>
          <h1>KnockKnock</h1>
          <p>Create your account to get started</p>
        </div>

        <div className="auth-content">
          {/* Sub-selector for Roles */}
          <div className="role-selector">
            <button 
              type="button"
              className={`role-tab ${userRole === "visitor" ? "active" : ""}`}
              onClick={() => setUserRole("visitor")}
            >
              Visitor
            </button>
            <button 
              type="button"
              className={`role-tab ${userRole === "admin" ? "active" : ""}`}
              onClick={() => setUserRole("admin")}
            >
              Administrator
            </button>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="section-label">PERSONAL INFORMATION</div>
            
            <div className="input-group">
              <label>FULL NAME</label>
              <input type="text" name="fullName" placeholder="Juan Dela Cruz" onChange={handleInputChange} required />
            </div>

            <div className="grid-2">
              <div className="input-group">
                <label>EMAIL ADDRESS</label>
                <input type="email" name="email" placeholder="name@email.com" onChange={handleInputChange} required />
              </div>
              <div className="input-group">
                <label>CONTACT NUMBER</label>
                <input type="text" name="contactNumber" placeholder="09123456789" onChange={handleInputChange} required />
              </div>
            </div>

            {/* Condo Specific Fields - Only for Administrator role */}
            {userRole === "admin" && (
              <div className="admin-fields-wrapper">
                <div className="section-label">CONDOMINIUM DETAILS</div>
                <div className="input-group">
                  <label>CONDOMINIUM NAME</label>
                  <input type="text" name="condoName" placeholder="e.g. Makati Tower" onChange={handleInputChange} required />
                </div>
                <div className="input-group">
                  <label>CONDOMINIUM ADDRESS</label>
                  <input type="text" name="condoAddress" placeholder="123 Ayala Avenue..." onChange={handleInputChange} required />
                </div>
              </div>
            )}

            <div className="grid-2">
              <div className="input-group">
                <label>PASSWORD</label>
                <input type="password" name="password" placeholder="********" onChange={handleInputChange} required />
              </div>
              <div className="input-group">
                <label>CONFIRM PASSWORD</label>
                <input type="password" name="confirmPassword" placeholder="********" onChange={handleInputChange} required />
              </div>
            </div>

            <button type="submit" className="primary-btn">
              {userRole === "admin" ? "Register Condominium" : "Create Account"}
            </button>
          </form>

          <div className="divider"><span>OR CONTINUE WITH</span></div>
          <button type="button" className="google-btn">
            <img
              src="https://fonts.gstatic.com/s/i/productlogos/googleg/v6/24px.svg"
              alt="Google"
            />
            Google Account
          </button>

          <p className="signup-text">
            Already have an account? <Link to="/" className="link">Sign In</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
