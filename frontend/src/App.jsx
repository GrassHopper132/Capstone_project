import { Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import ProtectedRoute, { STAFF_ROLES } from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ArtifactList from "./pages/ArtifactList";
import ArtifactDetail from "./pages/ArtifactDetail";
import ArtifactNew from "./pages/ArtifactNew";
import ExhibitionList from "./pages/ExhibitionList";
import ExhibitionDetail from "./pages/ExhibitionDetail";
import Visit from "./pages/Visit";
import Register from "./pages/Register";
import NotFound from "./pages/NotFound";

export default function App() {
    return (
        <Routes>
            {/* Open to anyone, no token required. */}
            <Route path="/visit" element={<Visit />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/*
        The whole staff application is gated on role, not merely on being
        signed in. A visitor account is authenticated and still has no business
        here, and the server would refuse every call it made.
      */}
            <Route
                element={
                    <ProtectedRoute roles={STAFF_ROLES}>
                        <Layout />
                    </ProtectedRoute>
                }
            >
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