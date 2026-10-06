import { api } from "./client";

/** Open to anyone. No token is sent or required. */
export const listOpenExhibitions = () => api.get("/public/exhibitions");
export const getOpenExhibition = (id) => api.get(`/public/exhibitions/${id}`);

/** Signed-in visitors only: the full programme, past and announced. */
export const listArchive = () => api.get("/visitor/exhibitions");

/**
 * Where the museum is. Replace with the real address before any public use;
 * the directions link is built from this string alone.
 */
export const MUSEUM = {
  name: "Museum Artifact Manager",
  address: "1 Harbor Road, Springfield",
  hours: "Tuesday to Sunday, 10am to 5pm",
};

export function directionsUrl() {
  const q = encodeURIComponent(`${MUSEUM.name}, ${MUSEUM.address}`);
  return `https://www.google.com/maps/dir/?api=1&destination=${q}`;
}

/** "2027-01-31" -> "31 January 2027", without timezone drift. */
export function longDate(iso) {
  if (!iso) return "";
  const [y, m, d] = iso.split("-").map(Number);
  const months = ["January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"];
  return `${d} ${months[m - 1]} ${y}`;
}