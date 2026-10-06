import { Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ArtifactList from "./pages/ArtifactList";
import ArtifactDetail from "./pages/ArtifactDetail";
import ArtifactNew from "./pages/ArtifactNew";
import ExhibitionList from "./pages/ExhibitionList";
import ExhibitionDetail from "./pages/ExhibitionDetail";
import Visit from "./pages/Visit";
import NotFound from "./pages/NotFound";

export default function App() {
  return (
    <Routes>
      {/* Open to anyone, no token required. */}
      <Route path="/visit" element={<Visit />} />
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="artifacts" element={<ArtifactList />} />
        <Route
          path="artifacts/new"
          element={
            <ProtectedRoute roles={["ADMIN", "CURATOR"]}>
              <ArtifactNew />
            </ProtectedRoute>
          }
        />
        <Route path="artifacts/:id" element={<ArtifactDetail />} />
        <Route path="exhibitions" element={<ExhibitionList />} />
        <Route path="exhibitions/:id" element={<ExhibitionDetail />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}