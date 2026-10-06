import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createArtifact } from "../api/artifacts";
import { listCollections, listLocations } from "../api/reference";
import { useApp } from "../context/AppContext";
import Field from "../components/Field";
import Spinner from "../components/Spinner";
import ErrorBanner from "../components/ErrorBanner";

const EMPTY = {
  accessionNumber: "", title: "", originCulture: "", datePeriod: "",
  material: "", description: "", collectionId: "", locationId: "", acquiredOn: "",
};

function validate(form) {
  const errors = {};
  if (!form.accessionNumber.trim()) errors.accessionNumber = "Accession number is required.";
  if (!form.title.trim()) errors.title = "Title is required.";
  if (!form.collectionId) errors.collectionId = "Choose a collection.";
  if (!form.locationId) errors.locationId = "Choose a storage location.";
  if (form.acquiredOn && form.acquiredOn > new Date().toISOString().slice(0, 10)) {
    errors.acquiredOn = "Acquisition date cannot be in the future.";
  }
  return errors;
}

export default function ArtifactNew() {
  const navigate = useNavigate();
  const { notify } = useApp();

  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  const [collections, setCollections] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loadingRefs, setLoadingRefs] = useState(true);
  const [refError, setRefError] = useState(null);

  useEffect(() => {
    let active = true;
    (async () => {
      setLoadingRefs(true);
      setRefError(null);
      try {
        const [cols, locs] = await Promise.all([listCollections(), listLocations()]);
        if (active) {
          setCollections(cols);
          setLocations(locs);
        }
      } catch (err) {
        if (active) setRefError(err);
      } finally {
        if (active) setLoadingRefs(false);
      }
    })();
    return () => { active = false; };
  }, []);

  const set = (name) => (e) => setForm((f) => ({ ...f, [name]: e.target.value }));

  async function handleSubmit(event) {
    event.preventDefault();
    const found = validate(form);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setSaving(true);
    try {
      const created = await createArtifact({
        ...form,
        collectionId: Number(form.collectionId),
        locationId: Number(form.locationId),
        acquiredOn: form.acquiredOn || null,
      });
      notify("success", `${created.accessionNumber} was accessioned.`);
      navigate(`/artifacts/${created.id}`);
    } catch (err) {
      if (err.status === 409) {
        setErrors({ accessionNumber: err.message });
      } else if (err.status === 400 && err.body?.validationErrors) {
        setErrors(err.body.validationErrors);
      } else {
        notify("error", err.message);
      }
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <div className="page-head">
        <div>
          <p className="page-eyebrow">/artifacts/new</p>
          <h1>Accession an artifact</h1>
        </div>
      </div>

      <ErrorBanner error={refError} />
      {loadingRefs && <Spinner label="Loading collections and locations" />}

      {!loadingRefs && (
        <form className="card" onSubmit={handleSubmit} noValidate>
          <div className="form-grid">
            <Field id="accessionNumber" label="Accession number" required error={errors.accessionNumber}
                   hint="Unique and permanent, e.g. 1994.22.7">
              <input id="accessionNumber" value={form.accessionNumber} onChange={set("accessionNumber")} />
            </Field>
            <Field id="title" label="Title" required error={errors.title}>
              <input id="title" value={form.title} onChange={set("title")} />
            </Field>

            <Field id="collectionId" label="Collection" required error={errors.collectionId}>
              <select id="collectionId" value={form.collectionId} onChange={set("collectionId")}>
                <option value="">Select a collection</option>
                {collections.map((c) => (
                  <option key={c.id} value={c.id}>{c.name} ({c.artifactCount})</option>
                ))}
              </select>
            </Field>
            <Field id="locationId" label="Storage location" required error={errors.locationId}>
              <select id="locationId" value={form.locationId} onChange={set("locationId")}>
                <option value="">Select a location</option>
                {locations.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.label}{l.climateControlled ? " - climate controlled" : ""}
                  </option>
                ))}
              </select>
            </Field>

            <Field id="originCulture" label="Origin culture" error={errors.originCulture}>
              <input id="originCulture" value={form.originCulture} onChange={set("originCulture")} />
            </Field>
            <Field id="datePeriod" label="Date or period" error={errors.datePeriod}>
              <input id="datePeriod" value={form.datePeriod} onChange={set("datePeriod")} />
            </Field>
            <Field id="material" label="Material" error={errors.material}>
              <input id="material" value={form.material} onChange={set("material")} />
            </Field>
            <Field id="acquiredOn" label="Acquired on" error={errors.acquiredOn}>
              <input id="acquiredOn" type="date" value={form.acquiredOn} onChange={set("acquiredOn")} />
            </Field>
          </div>

          <Field id="description" label="Description" error={errors.description}>
            <textarea id="description" rows="3" value={form.description} onChange={set("description")} />
          </Field>

          <div className="row-actions">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? "Saving..." : "Accession artifact"}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate("/artifacts")}>Cancel</button>
          </div>
        </form>
      )}
    </>
  );
}