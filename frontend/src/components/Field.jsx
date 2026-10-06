/** Label + control + inline error, so every form field is wired for screen readers. */
export default function Field({ id, label, error, required, children, hint }) {
  return (
    <div className={error ? "field field-invalid" : "field"}>
      <label htmlFor={id}>
        {label}{required && <span aria-hidden="true"> *</span>}
      </label>
      {children}
      {hint && !error && <p className="field-hint">{hint}</p>}
      {error && <p className="field-error" id={`${id}-error`}>{error}</p>}
    </div>
  );
}