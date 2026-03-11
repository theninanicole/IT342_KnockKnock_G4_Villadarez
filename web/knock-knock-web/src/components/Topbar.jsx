import React, { useContext } from "react";
import "./Topbar.css";
import { AuthContext } from "../context/AuthContext";

export default function Topbar({ title }) {
  const { user } = useContext(AuthContext);

  const getInitials = (name) => name?.split(" ").map(n => n[0]).join("").toUpperCase() || "U";

  return (
    <div className="topbar">
      <h3>{title}</h3>
      <div className="user-profile">
        <div className="user-info">
          <span className="user-name">{user?.fullName}</span>
          <span className="user-role">{user?.role === "VISITOR" ? "Visitor" : "Condominium Admin"}</span>
        </div>
        <div className="avatar">
          {getInitials(user?.fullName)}
        </div>
      </div>
    </div>
  );
}