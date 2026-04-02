import { apiUrl } from "../config/api";
import type { AuthUser, LoginResponse } from "../types/auth";
import type {
  CreateMissionPayload,
  DashboardSummary,
  Equipe,
  EquipeDetail,
  Hero,
  HeroDetail,
  HeroiDisponibilidade,
  Mission,
  MissionDetail,
  MissionHistoryEntry,
  MissionStatus,
  MissionTestPayload
} from "../types/mission";

type Json = unknown;

const TOKEN_KEY = "central_lj_token";

export function setAuthToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

function baseHeaders(contentJson = false): HeadersInit {
  const h: Record<string, string> = { Accept: "application/json" };
  const t = getAuthToken();
  if (t) {
    h.Authorization = `Bearer ${t}`;
  }
  if (contentJson) {
    h["Content-Type"] = "application/json";
  }
  return h;
}

async function parseJsonResponse(res: Response): Promise<Json> {
  const text = await res.text();
  if (!res.ok) {
    let message = `HTTP ${res.status}`;
    if (text) {
      try {
        const body = JSON.parse(text) as { message?: string };
        if (body?.message) {
          message = `${message} — ${body.message}`;
        } else {
          message = `${message} — ${text}`;
        }
      } catch {
        message = `${message} — ${text}`;
      }
    }
    throw new Error(message);
  }
  if (!text) return null;
  try {
    return JSON.parse(text) as Json;
  } catch {
    return text;
  }
}

async function getJson(path: string): Promise<Json> {
  const res = await fetch(apiUrl(path), { headers: baseHeaders(false) });
  return parseJsonResponse(res);
}

async function postJson(path: string, body: unknown): Promise<Json> {
  const res = await fetch(apiUrl(path), {
    method: "POST",
    headers: baseHeaders(true),
    body: JSON.stringify(body)
  });
  return parseJsonResponse(res);
}

async function postJsonPublic(path: string, body: unknown): Promise<Json> {
  const res = await fetch(apiUrl(path), {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  return parseJsonResponse(res);
}

async function patchJson(path: string, body: unknown): Promise<Json> {
  const res = await fetch(apiUrl(path), {
    method: "PATCH",
    headers: baseHeaders(true),
    body: JSON.stringify(body)
  });
  return parseJsonResponse(res);
}

export type CreateHeroPayload = {
  nomeHeroico: string;
  nomeCivil?: string | null;
  especialidade: string;
  statusDisponibilidade: HeroiDisponibilidade;
  nivel: string;
  ativo: boolean;
  equipeId?: string | null;
};

export type CreateTeamPayload = {
  nome: string;
  especialidadePrincipal?: string | null;
  ativa: boolean;
};

export const api = {
  login: (email: string, password: string) =>
    postJsonPublic("/api/auth/login", {
      email: email.trim(),
      password
    }) as Promise<LoginResponse>,

  getAuthMe: () => getJson("/api/auth/me") as Promise<AuthUser>,

  listMyMissions: () => getJson("/api/me/missions") as Promise<Mission[]>,

  getHello: () => getJson("/api/hello"),
  getHealth: () => getJson("/api/health"),
  postMissionTest: (payload: MissionTestPayload) => postJsonPublic("/api/missions/test", payload),
  postPublishTestEvent: (mensagem?: string) =>
    postJsonPublic("/api/events/publish-test", { mensagem: mensagem ?? null }),

  listMissions: () => getJson("/api/missions") as Promise<Mission[]>,
  listMissionsByStatus: (status: MissionStatus) =>
    getJson(`/api/missions/status/${status}`) as Promise<Mission[]>,
  getMissionHistory: (id: string) =>
    getJson(`/api/missions/${id}/history`) as Promise<MissionHistoryEntry[]>,
  getDashboardSummary: () => getJson("/api/missions/dashboard/summary") as Promise<DashboardSummary>,
  getMissionDetail: (id: string) => getJson(`/api/missions/${id}`) as Promise<MissionDetail>,
  createMission: (payload: CreateMissionPayload) => postJson("/api/missions", payload) as Promise<Mission>,

  listHeroes: () => getJson("/api/heroes") as Promise<Hero[]>,
  getHero: (id: string) => getJson(`/api/heroes/${id}`) as Promise<HeroDetail>,
  createHero: (payload: CreateHeroPayload) => postJson("/api/heroes", payload) as Promise<Hero>,
  patchHeroAvailability: (id: string, disponibilidade: HeroiDisponibilidade) =>
    patchJson(`/api/heroes/${id}/availability`, { disponibilidade }) as Promise<Hero>,

  listTeams: () => getJson("/api/teams") as Promise<Equipe[]>,
  getTeam: (id: string) => getJson(`/api/teams/${id}`) as Promise<EquipeDetail>,
  createTeam: (payload: CreateTeamPayload) => postJson("/api/teams", payload) as Promise<Equipe>,

  assignMissionToHero: (missionId: string, heroiId: string, atribuidoPor?: string | null) =>
    patchJson(`/api/missions/${missionId}/assign-hero`, {
      heroiId,
      atribuidoPor: atribuidoPor ?? null
    }) as Promise<Mission>,

  assignMissionToTeam: (missionId: string, equipeId: string, atribuidoPor?: string | null) =>
    patchJson(`/api/missions/${missionId}/assign-team`, {
      equipeId,
      atribuidoPor: atribuidoPor ?? null
    }) as Promise<Mission>
};
