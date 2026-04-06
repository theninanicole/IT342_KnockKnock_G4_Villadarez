import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import VisitorDashboard from "./pages/VisitorDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import Profile from "./pages/Profile";
import AdminVisits from "./pages/AdminVisits";
import StatusHistory from "./pages/StatusHistory";
import VisitorMyVisits from "./pages/VisitorMyVisits";
import VisitorNotifications from "./pages/VisitorNotifications";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
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
                <VisitorMyVisits />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <VisitorNotifications />
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
                <AdminVisits />
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
      </BrowserRouter>
    </AuthProvider>
  );
}