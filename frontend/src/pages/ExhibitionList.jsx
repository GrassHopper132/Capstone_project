import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listExhibitions, createExhibition, phaseLabel } from "../api/exhibitions";
import { useAuth } from "../context/AuthContext";

const BLANK = { title: "", gallery: "", startDate: "", endDate: "" };

export default function ExhibitionList() {
  const { user } = useAuth();
  const canPlan = user?.role === "ADMIN" || user?.role === "CURATOR";

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [form, setForm] = useState(BLANK);
  const [saving, setSaving] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const page = await listExhibitions({ size: 50 });
      setRows(page?.content ?? []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleCreate(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await createExhibition(form);
      setForm(BLANK);
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  function field(name, value) {
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  return (
    <section>
      <header className="page-head">
        <p className="eyebrow">Programme</p>
        <h1>Exhibitions</h1>
        <p className="lede">
          A show is a date range. An artifact added to a show that is running today
          goes on display straight away, and returns to storage when the season ends.
        </p>
      </header>

      {error && <p role="alert" className="error-text">{error}</p>}

      {canPlan && (
        <form onSubmit={handleCreate} className="card stack">
          <h2>Schedule a new exhibition</h2>

          <label htmlFor="ex-title">Title</label>
          <input id="ex-title" required maxLength={200}
            value={form.title} onChange={(e) => field("title", e.target.value)} />

          <label htmlFor="ex-gallery">Gallery</label>
          <input id="ex-gallery" maxLength={120}
            value={form.gallery} onChange={(e) => field("gallery", e.target.value)} />

          <label htmlFor="ex-start">Opens</label>
          <input id="ex-start" type="date" required
            value={form.startDate} onChange={(e) => field("startDate", e.target.value)} />

          <label htmlFor="ex-end">Closes</label>
          <input id="ex-end" type="date" required
            value={form.endDate} onChange={(e) => field("endDate", e.target.value)} />

          <button type="submit" className="btn btn-secondary" disabled={saving}>
            {saving ? "Scheduling..." : "Schedule exhibition"}
          </button>
        </form>
      )}

      {loading && <p>Loading exhibitions...</p>}

      {!loading && rows.length === 0 && <p>No exhibitions scheduled yet.</p>}

      {!loading && rows.length > 0 && (
        <table>
          <caption>Scheduled exhibitions</caption>
          <thead>
            <tr>
              <th scope="col">Title</th>
              <th scope="col">Gallery</th>
              <th scope="col">Opens</th>
              <th scope="col">Closes</th>
              <th scope="col">Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((ex) => (
              <tr key={ex.id}>
                <td><Link to={`/exhibitions/${ex.id}`}>{ex.title}</Link></td>
                <td>{ex.gallery}</td>
                <td>{ex.startDate}</td>
                <td>{ex.endDate}</td>
                <td>{phaseLabel(ex.phase)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}