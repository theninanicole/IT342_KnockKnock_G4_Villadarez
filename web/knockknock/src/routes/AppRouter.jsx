import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import Login from "@pages/auth/Login";
import Register from "@pages/auth/Register";
import VisitorDashboard from "@pages/visitor/VisitorDashboard";
import MyVisits from "@pages/visitor/MyVisits";
import Notifications from "@pages/visitor/Notifications";
import AdminDashboard from "@pages/admin/AdminDashboard";
import AllVisits from "@pages/admin/AllVisits";
import StatusHistory from "@pages/admin/StatusHistory";
import Profile from "@pages/Profile";

const VISITOR_ONLY = ["VISITOR"];
const ADMIN_ONLY = ["CONDOMINIUM_ADMIN"];
const AUTHENTICATED_ROLES = ["VISITOR", "CONDOMINIUM_ADMIN"];

export default function AppRouter() {
  return (
    <>
      <ToastContainer />
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/visitor-home"
          element={
            <ProtectedRoute allowedRoles={VISITOR_ONLY}>
              <VisitorDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-visits"
          element={
            <ProtectedRoute allowedRoles={VISITOR_ONLY}>
              <MyVisits />
            </ProtectedRoute>
          }
        />
        <Route
          path="/notifications"
          element={
            <ProtectedRoute allowedRoles={VISITOR_ONLY}>
              <Notifications />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin-home"
          element={
            <ProtectedRoute allowedRoles={ADMIN_ONLY}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/all-visits"
          element={
            <ProtectedRoute allowedRoles={ADMIN_ONLY}>
              <AllVisits />
            </ProtectedRoute>
          }
        />
        <Route
          path="/status-history"
          element={
            <ProtectedRoute allowedRoles={ADMIN_ONLY}>
              <StatusHistory />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute allowedRoles={AUTHENTICATED_ROLES}>
              <Profile />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
