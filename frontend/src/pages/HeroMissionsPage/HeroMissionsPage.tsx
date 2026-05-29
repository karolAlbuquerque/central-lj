import { Navigate } from "react-router-dom";

/** Redireciona para a lista unificada de missões. */
export function HeroMissionsPage() {
  return <Navigate to="/missoes" replace />;
}
