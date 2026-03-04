import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import RegisterVisitor from "./pages/RegisterVisitor";
import RegisterAdmin from "./pages/RegisterAdmin";
import VisitorDashboard from "./pages/VisitorDashboard";
import AdminDashboard from "./pages/AdminDashboard";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/register-visitor" element={<RegisterVisitor />} />
          <Route path="/register-admin" element={<RegisterAdmin />} />

          <Route
            path="/visitor-dashboard"
            element={
              <ProtectedRoute role="visitor">
                <VisitorDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin-dashboard"
            element={
              <ProtectedRoute role="condominium_admin">
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}