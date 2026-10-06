import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { deleteArtifact, getArtifact, statusLabel } from "../api/artifacts";
import { useApp } from "../context/AppContext";
import Spinner from "../components/Spinner";
import ErrorBanner from "../components/ErrorBanner";

export default function ArtifactDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { notify } = useApp();

  const [artifact, setArtifact] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setArtifact(await getArtifact(id));
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { load(); }, [load]);

  async function handleDelete() {
    if (!window.confirm(`Deaccession ${artifact.accessionNumber}? This cannot be undone.`)) return;
    try {
      await deleteArtifact(id);
      notify("success", `${artifact.accessionNumber} was deaccessioned.`);
      navigate("/artifacts");
    } catch (err) {
      notify("error", err.message);
    }
  }

  if (loading) return <Spinner label="Loading artifact" />;
  if (error) return <ErrorBanner error={error} onRetry={load} />;

  const fields = [
    ["Origin culture", artifact.originCulture],
    ["Date or period", artifact.datePeriod],
    ["Material", artifact.material],
    ["Acquired", artifact.acquiredOn],
    ["Collection", artifact.collectionName],
    ["Location", artifact.locationLabel],
  ];

  return (
    <>
      <div className="page-head">
        <div>
          <p className="page-eyebrow">Record #{artifact.accessionNumber}</p>
          <h1>{artifact.title}</h1>
        </div>
        <div className="row-actions">
          <Link className="btn btn-secondary" to="/artifacts">Back to list</Link>
          <button type="button" className="btn btn-danger" onClick={handleDelete}>Deaccession</button>
        </div>
      </div>

      <div className="card">
        <p><span className="pill">{statusLabel(artifact.status)}</span></p>
        <div className="detail-grid" style={{ marginTop: "1.25rem" }}>
          {fields.map(([label, value]) => (
            <div key={label}>
              <p className="detail-label">{label}</p>
              <p className="detail-value">{value || "Not recorded"}</p>
            </div>
          ))}
        </div>
        {artifact.description && (
          <div style={{ marginTop: "1.5rem" }}>
            <p className="detail-label">Description</p>
            <p className="detail-value">{artifact.description}</p>
          </div>
        )}
      </div>
    </>
  );
}