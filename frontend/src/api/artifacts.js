import { api } from "./client";

export function listArtifacts({ page = 0, size = 20, status, material, search } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (status) params.set("status", status);
  if (material) params.set("material", material);
  if (search) params.set("search", search);
  return api.get(`/artifacts?${params.toString()}`);
}

export const getArtifact = (id) => api.get(`/artifacts/${id}`);
export const createArtifact = (data) => api.post("/artifacts", data);
export const updateArtifact = (id, data) => api.put(`/artifacts/${id}`, data);
export const deleteArtifact = (id) => api.del(`/artifacts/${id}`);

export const STATUSES = ["STORED", "ON_DISPLAY", "IN_RESTORATION", "DEACCESSIONED"];

export function statusLabel(status) {
  return status
    ? status.toLowerCase().split("_").map((w) => w[0].toUpperCase() + w.slice(1)).join(" ")
    : "";
}