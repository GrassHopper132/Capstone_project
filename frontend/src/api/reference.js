import { api } from "./client";

export const listCollections = () => api.get("/collections");
export const listLocations = () => api.get("/locations");