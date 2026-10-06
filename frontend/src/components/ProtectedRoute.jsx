import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Sends anonymous visitors to the login screen, and blocks the wrong role. */
export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (roles && !roles.includes(user?.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}