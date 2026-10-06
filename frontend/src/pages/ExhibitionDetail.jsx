import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  getExhibition,
  addArtifactToExhibition,
  removeArtifactFromExhibition,
  phaseLabel,
} from "../api/exhibitions";
import { listArtifacts, statusLabel } from "../api/artifacts";
import { useAuth } from "../context/AuthContext";

export default function ExhibitionDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const canPlan = user?.role === "ADMIN" || user?.role === "CURATOR";

  const [exhibition, setExhibition] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [chosen, setChosen] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const detail = await getExhibition(id);
      setExhibition(detail);
      const page = await listArtifacts({ size: 200 });
      setCandidates(page?.content ?? []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [id]);

  async function handleAdd(event) {
    event.preventDefault();
    if (!chosen) return;
    setBusy(true);
    setError(null);
    try {
      const updated = await addArtifactToExhibition(id, Number(chosen));
      setExhibition(updated);
      setChosen("");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleRemove(artifactId) {
    setBusy(true);
    setError(null);
    try {
      const updated = await removeArtifactFromExhibition(id, artifactId);
      setExhibition(updated);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <p>Loading exhibition...</p>;
  if (!exhibition) return <p role="alert" className="error-text">{error ?? "Not found."}</p>;

  const onShow = new Set((exhibition.artifacts ?? []).map((a) => a.artifactId));
  const available = candidates.filter((a) => !onShow.has(a.id));

  return (
    <section>
      <p><Link to="/exhibitions">Back to exhibitions</Link></p>

      <header className="page-head">
        <p className="eyebrow">{phaseLabel(exhibition.phase)}</p>
        <h1>{exhibition.title}</h1>
        <p className="lede">
          {exhibition.gallery} &middot; {exhibition.startDate} to {exhibition.endDate}
        </p>
      </header>

      {error && <p role="alert" className="error-text">{error}</p>}

      {canPlan && (
        <form onSubmit={handleAdd} className="card stack">
          <h2>Add an object to this show</h2>
          <label htmlFor="pick">Artifact</label>
          <select id="pick" value={chosen} onChange={(e) => setChosen(e.target.value)}>
            <option value="">Choose an artifact</option>
            {available.map((a) => (
              <option key={a.id} value={a.id}>
                {a.accessionNumber} &mdash; {a.title} ({statusLabel(a.status)})
              </option>
            ))}
          </select>
          <button type="submit" className="btn btn-secondary" disabled={busy || !chosen}>
            {busy ? "Working..." : "Add to exhibition"}
          </button>
        </form>
      )}

      <h2>On show ({exhibition.artifacts?.length ?? 0})</h2>

      {(exhibition.artifacts ?? []).length === 0 && <p>No objects in this exhibition yet.</p>}

      {(exhibition.artifacts ?? []).length > 0 && (
        <table>
          <caption>Objects in display order</caption>
          <thead>
            <tr>
              <th scope="col">Order</th>
              <th scope="col">Accession</th>
              <th scope="col">Title</th>
              <th scope="col">Status</th>
              {canPlan && <th scope="col">Action</th>}
            </tr>
          </thead>
          <tbody>
            {exhibition.artifacts.map((a) => (
              <tr key={a.artifactId}>
                <td>{a.displayOrder}</td>
                <td><Link to={`/artifacts/${a.artifactId}`}>{a.accessionNumber}</Link></td>
                <td>{a.title}</td>
                <td>{statusLabel(a.status)}</td>
                {canPlan && (
                  <td>
                    <button type="button" className="btn btn-ghost" disabled={busy}
                      onClick={() => handleRemove(a.artifactId)}>
                      Remove
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}