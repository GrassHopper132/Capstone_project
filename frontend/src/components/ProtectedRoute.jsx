import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** The roles that may use the staff application at all. */
export const STAFF_ROLES = ["ADMIN", "CURATOR", "RESTORER"];

/** Sends anonymous visitors to the login screen, and blocks the wrong role. */
export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (roles && !roles.includes(user?.role)) {
    /*
     * Where a refused user belongs depends on who they are. A curator turned
     * away from an admin-only screen still has the dashboard. A visitor has
     * nothing in the staff application at all, so sending them to "/" would
     * bounce them straight back to this guard and loop.
     */
    const home = STAFF_ROLES.includes(user?.role) ? "/" : "/visit";
    return <Navigate to={home} replace />;
  }

  return children;
}