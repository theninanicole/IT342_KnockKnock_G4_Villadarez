import React, { useContext } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import { Plus, Files } from "lucide-react"; //
import "./Dashboard.css";
import { AuthContext } from "../context/AuthContext";

export default function VisitorDashboard() {
  const { user } = useContext(AuthContext);

  return (
    <div className="dashboard-layout">
      <Sidebar role="visitor" />
      <div className="main-content">
        <TopBar title="Dashboard" />
        <div className="content-inner">
          <div className="welcome-card">
            <div className="welcome-text">
              <h1>Welcome Back, {(user?.fullName?.split(" ")[0]) || "User"}!</h1>
              <button className="new-visit-btn">
                <span>+ New Visit</span>
              </button>
            </div>
            {/* SVG Illustration replaces emoji */}
            <div className="welcome-illustration">
              <Files size={120} strokeWidth={1} />
            </div>
          </div>

          <div className="dashboard-section">
            <h4>Upcoming Visits</h4>
            <div className="visit-card empty-state">
              <p>No upcoming visits found.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}