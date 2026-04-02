/**
 * Base URL da API (opcional).
 * Em desenvolvimento, deixe vazio: o Vite encaminha `/api` para `localhost:8080`.
 * Para build estático apontando para outro host, defina `VITE_API_BASE_URL` como **origem**
 * (ex.: `http://localhost:8080`) **sem** sufixo `/api` — os paths já começam com `/api/...`.
 */
export function apiUrl(path: string): string {
  let base = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim() ?? "";
  base = base.replace(/\/$/, "");
  const p = path.startsWith("/") ? path : `/${path}`;

  /*
   * Se VITE_API_BASE_URL termina em “/api” (erro comum), o resultado viraria /api/api/... e o
   * Spring retorna 404 para /api/auth/login. Normalizamos removendo o sufixo duplicado.
   */
  if (p.startsWith("/api") && (base === "/api" || base.endsWith("/api"))) {
    base = base === "/api" ? "" : base.slice(0, -"/api".length);
  }

  return `${base}${p}`;
}
