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

export default function AppRouter() {
  return (
    <>
      <ToastContainer />
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/visitor-dashboard"
          element={
            <ProtectedRoute>
              <VisitorDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-visits"
          element={
            <ProtectedRoute>
              <MyVisits />
            </ProtectedRoute>
          }
        />
        <Route
          path="/notifications"
          element={
            <ProtectedRoute>
              <Notifications />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin-dashboard"
          element={
            <ProtectedRoute>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/all-visits"
          element={
            <ProtectedRoute>
              <AllVisits />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/history"
          element={
            <ProtectedRoute>
              <StatusHistory />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}