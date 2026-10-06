export default function EmptyState({ title, body, action }) {
  return (
    <div className="empty">
      <p className="empty-title">{title}</p>
      {body && <p className="empty-body">{body}</p>}
      {action}
    </div>
  );
}