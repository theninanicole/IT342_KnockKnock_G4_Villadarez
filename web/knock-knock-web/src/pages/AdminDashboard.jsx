import React, { useContext } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/Topbar";
import { Search, QrCode } from "lucide-react";
import "./Dashboard.css";
import { AuthContext } from "../context/AuthContext";

export default function AdminDashboard() {
  const { user } = useContext(AuthContext);

  return (
    <div className="dashboard-layout">
      <Sidebar role="ADMIN" />
      <div className="main-content">
        <TopBar title="Dashboard" />
        <div className="content-inner">
          <div className="verification-card">
            <h2>Verification</h2>
            <p>Scan QR or enter reference number presented by the visitor.</p>
            <div className="search-box-container">
              <div className="search-input-wrapper">
                <Search size={18} color="#94a3b8" />
                <input type="text" placeholder="Search reference number..." />
              </div>
              <button className="icon-action-btn">
                <QrCode size={30} />
              </button>
              <button className="verify-action-btn">Verify</button>
            </div>
          </div>

          <div className="dashboard-section">
            <div className="section-header">
              <div className="status-dot"></div>
              <h4>Currently Checked-In</h4>
            </div>
            {/* List rendered here from */}
          </div>
        </div>
      </div>
    </div>
  );
}