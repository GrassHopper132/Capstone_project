const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";
const TOKEN_KEY = "mam.token";

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || `Request failed with status ${status}`);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

async function request(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };

  const token = tokenStore.get();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  let body = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = { message: text };
    }
  }

  if (!response.ok) {
    throw new ApiError(response.status, body);
  }
  return body;
}

export const api = {
  get: (path) => request(path),
  post: (path, data) => request(path, { method: "POST", body: JSON.stringify(data) }),
  put: (path, data) => request(path, { method: "PUT", body: JSON.stringify(data) }),
  del: (path) => request(path, { method: "DELETE" }),
};