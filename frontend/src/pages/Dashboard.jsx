import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { listArtifacts, statusLabel } from "../api/artifacts";
import Spinner from "../components/Spinner";
import ErrorBanner from "../components/ErrorBanner";

export default function Dashboard() {
  const [artifacts, setArtifacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await listArtifacts({ size: 100 });
      setArtifacts(page.content ?? []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const counts = useMemo(() => {
    const byStatus = { STORED: 0, ON_DISPLAY: 0, IN_RESTORATION: 0, DEACCESSIONED: 0 };
    artifacts.forEach((a) => { byStatus[a.status] = (byStatus[a.status] ?? 0) + 1; });
    return byStatus;
  }, [artifacts]);

  return (
    <>
      <div className="page-head">
        <div>
          <p className="page-eyebrow">Overview</p>
          <h1>Dashboard</h1>
        </div>
        <Link className="btn btn-primary" to="/artifacts/new">
          Accession artifact
        </Link>
      </div>

      <ErrorBanner error={error} onRetry={load} />
      {loading && <Spinner label="Loading collection summary" />}

      {!loading && !error && (
        <>
          <div className="kpi-grid">
            <div className="card">
              <p className="kpi-label">Total artifacts</p>
              <p className="kpi-value">{artifacts.length}</p>
            </div>
            <div className="card">
              <p className="kpi-label">On display</p>
              <p className="kpi-value">{counts.ON_DISPLAY}</p>
            </div>
            <div className="card">
              <p className="kpi-label">In restoration</p>
              <p className="kpi-value">{counts.IN_RESTORATION}</p>
            </div>
            <div className="card">
              <p className="kpi-label">In storage</p>
              <p className="kpi-value">{counts.STORED}</p>
            </div>
          </div>

          <div className="card">
            <h2>Recently accessioned</h2>
            <table>
              <caption className="sr-only">Five most recent artifacts</caption>
              <thead>
                <tr><th>Accession</th><th>Title</th><th>Status</th><th>Collection</th></tr>
              </thead>
              <tbody>
                {artifacts.slice(0, 5).map((a) => (
                  <tr key={a.id}>
                    <td data-label="Accession"><Link to={`/artifacts/${a.id}`}>{a.accessionNumber}</Link></td>
                    <td data-label="Title">{a.title}</td>
                    <td data-label="Status"><span className="pill">{statusLabel(a.status)}</span></td>
                    <td data-label="Collection">{a.collectionName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </>
  );
}