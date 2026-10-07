import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Field from "../components/Field";

/**
 * Public registration. Every account created here is a visitor, whatever the
 * API is asked for, so the form does not offer a role at all: offering a choice
 * the server ignores would be dishonest.
 */
export default function Register() {
  const { signUp } = useAuth();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    if (password.length < 8) {
      setError("Choose a password of at least eight characters.");
      return;
    }
    if (password !== confirm) {
      setError("The two passwords do not match.");
      return;
    }

    setBusy(true);
    try {
      await signUp({ fullName, email, password });
      navigate("/visit", { replace: true });
    } catch (err) {
      if (err.status === 409) {
        setError("An account already exists for that address. Sign in instead.");
      } else {
        setError(err.message);
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-shell">
      <form className="card login-card" onSubmit={handleSubmit} noValidate>
        <p className="page-eyebrow">Museum Artifact Manager</p>
        <h1>Create an account</h1>

        <p style={{ marginTop: 0, fontSize: "0.9375rem", lineHeight: 1.6, color: "#5c574d" }}>
          A free account opens the full programme: shows that have closed, and
          shows announced but not yet installed.
        </p>

        {error && <div className="banner banner-error" role="alert"><p>{error}</p></div>}

        <Field id="fullName" label="Name" required>
          <input id="fullName" type="text" autoComplete="name" maxLength={120}
                 value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </Field>

        <Field id="email" label="Email" required>
          <input id="email" type="email" autoComplete="username" maxLength={120}
                 value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>

        <Field id="password" label="Password" required>
          <input id="password" type="password" autoComplete="new-password"
                 value={password} onChange={(e) => setPassword(e.target.value)} />
        </Field>

        <Field id="confirm" label="Password again" required>
          <input id="confirm" type="password" autoComplete="new-password"
                 value={confirm} onChange={(e) => setConfirm(e.target.value)} />
        </Field>

        <button type="submit" className="btn btn-primary" disabled={busy} style={{ width: "100%" }}>
          {busy ? "Creating your account..." : "Create account"}
        </button>

        <p style={{ marginBottom: 0, fontSize: "0.9375rem" }}>
          Already have one? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}