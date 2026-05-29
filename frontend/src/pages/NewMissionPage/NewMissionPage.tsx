import { Navigate } from "react-router-dom";

/** Redireciona criação legada para nova missão (herói). */
export function NewMissionPage() {
  return <Navigate to="/missoes/nova" replace />;
}
