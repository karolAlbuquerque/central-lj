import { Navigate, useParams } from "react-router-dom";

/** Redireciona detalhe legado para a sala de comando da missão. */
export function MissionDetailPage() {
  const { id } = useParams<{ id: string }>();
  if (!id) return <Navigate to="/missoes" replace />;
  return <Navigate to={`/missoes/${id}`} replace />;
}
