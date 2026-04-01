/**
 * Base URL da API (opcional).
 * Em desenvolvimento, deixe vazio: o Vite encaminha `/api` para `localhost:8080`.
 * Para build estático apontando para outro host, defina `VITE_API_BASE_URL`.
 */
export function apiUrl(path: string): string {
  const raw = import.meta.env.VITE_API_BASE_URL as string | undefined;
  const base = raw?.replace(/\/$/, "") ?? "";
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${base}${p}`;
}
