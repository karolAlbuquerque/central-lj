import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

export function RequireAuth() {
  const { user, bootstrapping } = useAuth();
  const location = useLocation();

  if (bootstrapping) {
    return (
      <div style={{ padding: 28, color: "var(--muted)" }}>
        <p>Carregando sessão…</p>
      </div>
    );
  }

  if (!user) {
    const next = encodeURIComponent(location.pathname + location.search);
    return (
      <Navigate
        to={`/login?reason=session_expired&next=${next}`}
        replace
        state={{ from: location.pathname }}
      />
    );
  }

  return <Outlet />;
}
