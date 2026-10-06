import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listArtifacts, statusLabel, STATUSES } from "../api/artifacts";
import Spinner from "../components/Spinner";
import ErrorBanner from "../components/ErrorBanner";
import EmptyState from "../components/EmptyState";

export default function ArtifactList() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await listArtifacts({ page, size: 20, search: search || undefined, status: status || undefined }));
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [page, search, status]);

  useEffect(() => { load(); }, [load]);

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <div className="page-head">
        <div>
          <p className="page-eyebrow">/artifacts</p>
          <h1>Artifacts</h1>
        </div>
        <Link className="btn btn-primary" to="/artifacts/new">
          Accession artifact
        </Link>
      </div>

      <form className="toolbar" onSubmit={(e) => { e.preventDefault(); setPage(0); load(); }}>
        <div className="field" style={{ marginBottom: 0 }}>
          <label htmlFor="search">Search</label>
          <input id="search" type="search" value={search} placeholder="Title or accession number"
                 onChange={(e) => setSearch(e.target.value)} style={{ width: "240px" }} />
        </div>
        <div className="field" style={{ marginBottom: 0 }}>
          <label htmlFor="status">Status</label>
          <select id="status" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
            <option value="">All</option>
            {STATUSES.map((s) => <option key={s} value={s}>{statusLabel(s)}</option>)}
          </select>
        </div>
        <button type="submit" className="btn btn-secondary">Apply</button>
      </form>

      <ErrorBanner error={error} onRetry={load} />
      {loading && <Spinner label="Loading artifacts" />}

      {!loading && !error && rows.length === 0 && (
        <EmptyState
          title="No artifacts match these filters."
          body="Try clearing the search term or choosing a different status."
          action={<button type="button" className="btn btn-secondary"
                          onClick={() => { setSearch(""); setStatus(""); setPage(0); }}>Reset filters</button>}
        />
      )}

      {!loading && !error && rows.length > 0 && (
        <div className="card">
          <table>
            <thead>
              <tr><th>Accession</th><th>Title</th><th>Material</th><th>Status</th><th>Location</th></tr>
            </thead>
            <tbody>
              {rows.map((a) => (
                <tr key={a.id}>
                  <td data-label="Accession"><Link to={`/artifacts/${a.id}`}>{a.accessionNumber}</Link></td>
                  <td data-label="Title">{a.title}</td>
                  <td data-label="Material">{a.material}</td>
                  <td data-label="Status"><span className="pill">{statusLabel(a.status)}</span></td>
                  <td data-label="Location">{a.locationLabel}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="toolbar" style={{ marginTop: "1rem", marginBottom: 0, justifyContent: "space-between" }}>
            <span className="page-eyebrow">Page {page + 1} of {totalPages} — {data.totalElements} total</span>
            <div className="row-actions">
              <button type="button" className="btn btn-secondary" disabled={page === 0}
                      onClick={() => setPage((p) => p - 1)}>Previous</button>
              <button type="button" className="btn btn-secondary" disabled={page + 1 >= totalPages}
                      onClick={() => setPage((p) => p + 1)}>Next</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}