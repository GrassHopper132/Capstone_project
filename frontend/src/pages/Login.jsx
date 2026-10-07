import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Field from "../components/Field";

export default function Login() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await signIn(email, password);
      navigate(location.state?.from?.pathname ?? "/", { replace: true });
    } catch (err) {
      setError(err.status === 401 ? "Email or password is incorrect." : err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-shell">
      <form className="card login-card" onSubmit={handleSubmit} noValidate>
        <p className="page-eyebrow">Museum Artifact Manager</p>
        <h1>Sign in</h1>

        {error && <div className="banner banner-error" role="alert"><p>{error}</p></div>}

        <Field id="email" label="Email" required>
          <input id="email" type="email" autoComplete="username"
                 value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>

        <Field id="password" label="Password" required>
          <input id="password" type="password" autoComplete="current-password"
                 value={password} onChange={(e) => setPassword(e.target.value)} />
        </Field>

        <button type="submit" className="btn btn-primary" disabled={busy} style={{ width: "100%" }}>
          {busy ? "Signing in..." : "Sign in"}
        </button>
      </form>
    </div>
  );
}