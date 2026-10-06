export default function ErrorBanner({ error, onRetry }) {
  if (!error) return null;
  const status = error.status ? `${error.status} — ` : "";
  return (
    <div className="banner banner-error" role="alert">
      <div>
        <strong>Something went wrong.</strong>
        <p>{status}{error.message}</p>
      </div>
      {onRetry && (
        <button type="button" className="btn btn-secondary" onClick={onRetry}>Try again</button>
      )}
    </div>
  );
}