import { Link } from "react-router-dom";
import EmptyState from "../components/EmptyState";

export default function NotFound() {
  return (
    <>
      <div className="page-head">
        <div>
          <p className="page-eyebrow">404</p>
          <h1>Page not found</h1>
        </div>
      </div>
      <EmptyState
        title="That page does not exist."
        body="The link may be out of date, or the record may have been deaccessioned."
        action={<Link className="btn btn-primary" to="/">Back to dashboard</Link>}
      />
    </>
  );
}