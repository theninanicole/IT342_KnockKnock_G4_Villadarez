import React, { useContext } from "react";
import { ShieldCheck,LayoutDashboard, CalendarDays, Bell, User, LogOut, History, ClipboardList } from "lucide-react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import "./Sidebar.css";
import { AuthContext } from "../context/AuthContext";

export default function Sidebar({ role }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useContext(AuthContext);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const currentRole = role?.toUpperCase();
  const navItems = currentRole === "VISITOR" 
    ? [
        { name: "Dashboard", path: "/visitor-dashboard", icon: <LayoutDashboard size={20} /> },
        { name: "My Visits", path: "/my-visits", icon: <CalendarDays size={20} /> },
        { name: "Notifications", path: "/notifications", icon: <Bell size={20} /> },
      ]
    : [
        { name: "Dashboard", path: "/admin-dashboard", icon: <LayoutDashboard size={20} /> },
        { name: "All Visits", path: "/admin/all-visits", icon: <ClipboardList size={20} /> },
        { name: "Status History", path: "/admin/history", icon: <History size={20} /> },
      ];

  return (
    <div className="sidebar">
        <div className="sidebar-brand">
            <div className="brand-icon">
                <ShieldCheck size={22} color="white" strokeWidth={1.5} />
            </div>
        <h2>KnockKnock</h2>
        </div>

      <div className="sidebar-section">
        <p className="section-title">GENERAL</p>
        {navItems.map((item) => (
          <Link 
            key={item.name} 
            to={item.path} 
            className={`nav-item ${location.pathname === item.path ? "active" : ""}`}
          >
            {item.icon}
            <span>{item.name}</span>
          </Link>
        ))}
      </div>

      <div className="sidebar-footer">
        <Link to="/profile" className="nav-item">
          <User size={20} />
          <span>Account</span>
        </Link>
        <button onClick={handleLogout} className="nav-item logout-btn">
          <LogOut size={20} />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}