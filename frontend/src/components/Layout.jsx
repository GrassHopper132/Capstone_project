import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useApp } from "../context/AppContext";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
  const { notice, dismiss, theme, toggleTheme } = useApp();
  const { user, signOut } = useAuth();
  const navigate = useNavigate();

  const canAccession = user?.role === "ADMIN" || user?.role === "CURATOR";

  const nav = [
    { to: "/", label: "Dashboard", end: true, show: true },
    { to: "/artifacts", label: "Artifacts", show: true },
    { to: "/artifacts/new", label: "Accession", show: canAccession },
  ].filter((item) => item.show);

  function handleSignOut() {
    signOut();
    navigate("/login", { replace: true });
  }

  return (
    <div className="shell" data-theme={theme}>
      <a className="skip-link" href="#main">Skip to content</a>

      <aside className="sidebar">
        <div className="brand">
          <span className="brand-eyebrow">Collection</span>
          <span className="brand-name">Museum Artifact Manager</span>
        </div>

        <nav aria-label="Main">
          {nav.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end}
              className={({ isActive }) => (isActive ? "nav-link is-active" : "nav-link")}>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-foot">
          {user && (
            <div className="who">
              <span className="who-name">{user.fullName}</span>
              <span className="who-role">{user.role}</span>
            </div>
          )}
          <button type="button" className="btn btn-ghost" onClick={toggleTheme}>
            {theme === "light" ? "Dark mode" : "Light mode"}
          </button>
          <button type="button" className="btn btn-secondary" onClick={handleSignOut}>Sign out</button>
        </div>
      </aside>

      <main id="main" className="content">
        {notice && (
          <div className={`notice notice-${notice.kind}`} role="status">
            <span>{notice.text}</span>
            <button type="button" className="notice-close" onClick={dismiss} aria-label="Dismiss message">
              &times;
            </button>
          </div>
        )}
        <Outlet />
      </main>
    </div>
  );
}