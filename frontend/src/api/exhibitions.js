import { api } from "./client";

export function listExhibitions({ page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  return api.get(`/exhibitions?${params.toString()}`);
}

export const listCurrentExhibitions = () => api.get("/exhibitions/current");
export const getExhibition = (id) => api.get(`/exhibitions/${id}`);
export const createExhibition = (data) => api.post("/exhibitions", data);
export const updateExhibition = (id, data) => api.put(`/exhibitions/${id}`, data);
export const deleteExhibition = (id) => api.del(`/exhibitions/${id}`);

export const addArtifactToExhibition = (id, artifactId) =>
  api.post(`/exhibitions/${id}/artifacts`, { artifactId });

export const removeArtifactFromExhibition = (id, artifactId) =>
  api.del(`/exhibitions/${id}/artifacts/${artifactId}`);

/** UPCOMING / CURRENT / PAST, derived server side from the date range. */
export function phaseLabel(phase) {
  if (phase === "CURRENT") return "On now";
  if (phase === "UPCOMING") return "Upcoming";
  if (phase === "PAST") return "Closed";
  return phase ?? "";
}