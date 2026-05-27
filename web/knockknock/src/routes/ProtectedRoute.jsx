import { useContext } from "react";
import { AuthContext } from "@context/AuthContext";
import { Navigate } from "react-router-dom";

const ROLE_HOME = {
  VISITOR: "/visitor-home",
  CONDOMINIUM_ADMIN: "/admin-home",
};

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user } = useContext(AuthContext);

  if (!user) {
    return <Navigate to="/" replace />;
  }

  const userRole = user.role?.toUpperCase();
  const normalizedAllowedRoles = allowedRoles?.map((role) => role.toUpperCase());

  if (normalizedAllowedRoles?.length && !normalizedAllowedRoles.includes(userRole)) {
    return <Navigate to={ROLE_HOME[userRole] || "/"} replace />;
  }

  return children;
}
