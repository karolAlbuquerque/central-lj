import { apiUrl } from "../config/api";
import type { AuthUser, LoginResponse } from "../types/auth";
import type {
  Equipe,
  EquipeDetail,
  Hero,
  HeroDetail,
  HeroiDisponibilidade,
  MissionTestPayload
} from "../types/mission";
import type {
  CreateMissionPayload,
  CreateTaskPayload,
  ExecuteTaskPayload,
  DuelSession,
  Mission,
  MissionDetail,
  MissionMember,
  MissionTask,
  PuzzleProgressPayload,
  SabotageScope,
  SabotageType
} from "../types/pvp";

type Json = unknown;

function normalizeDuelSession(raw: DuelSession): DuelSession {
  return {
    ...raw,
    infiltrationProgress: raw.infiltrationProgress ?? 0,
    infiltrationRequired: raw.infiltrationRequired ?? 3,
    roundCurrent: raw.roundCurrent ?? 1,
    roundMax: raw.roundMax ?? 1,
    attackerRoundsWon: raw.attackerRoundsWon ?? 0,
    defenderRoundsWon: raw.defenderRoundsWon ?? 0
  };
}

export const TOKEN_KEY = "central_lj_token";
export const SESSION_EXPIRED_EVENT = "central-lj:session-expired";

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

function friendlyApiMessage(status: number, body: { message?: string; errors?: Record<string, string> }): string {
  const field = body.errors ? Object.keys(body.errors)[0] : undefined;
  const raw = (field && body.errors?.[field]) || body.message || "";
  const lower = raw.toLowerCase();

  if (status === 401) {
    return "E-mail ou senha incorretos.";
  }
  if (lower.includes("must not be blank") || lower.includes("não deve estar em branco")) {
    if (field === "email") return "Informe seu e-mail.";
    if (field === "password") return "Informe sua senha.";
    if (field === "nome") return "Informe seu nome.";
    if (field === "role") return "Selecione o papel (herói ou vilão).";
    return "Preencha todos os campos obrigatórios.";
  }
  if (lower.includes("must be a well-formed email") || lower.includes("e-mail inválido")) {
    return "Informe um e-mail válido.";
  }
  if (raw) return raw;
  return `Erro HTTP ${status}`;
}

function shouldRedirectSessionExpired(requestPath: string): boolean {
  if (requestPath.includes("/api/auth/login") || requestPath.includes("/api/auth/register")) {
    return false;
  }
  if (requestPath.includes("/api/duels/")) {
    return false;
  }
  if (typeof window === "undefined") return false;
  const path = window.location.pathname;
  if (path === "/login" || path === "/cadastro") return false;
  if (path.includes("/duelo") || path.includes("/vilao/duelo")) return false;
  if (!getAuthToken()) return false;
  return true;
}

async function parseJsonResponse(res: Response, requestPath = ""): Promise<Json> {
  const text = await res.text();
  if (!res.ok) {
    if (res.status === 401 && shouldRedirectSessionExpired(requestPath)) {
      setAuthToken(null);
      if (typeof window !== "undefined") {
        window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT));
      }
      throw new Error("Sessão expirada. Faça login novamente.");
    }
    let message = `HTTP ${res.status}`;
    if (text) {
      try {
        const body = JSON.parse(text) as { message?: string; errors?: Record<string, string> };
        message = friendlyApiMessage(res.status, body);
      } catch {
        message = `${message} — ${text}`;
      }
    } else if (res.status === 401) {
      message = friendlyApiMessage(401, {});
    } else if (res.status >= 500) {
      message =
        "Erro interno no servidor. Reinicie o backend (mvn clean spring-boot:run) e tente novamente.";
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
  return parseJsonResponse(res, path);
}

async function postJson(path: string, body: unknown): Promise<Json> {
  const res = await fetch(apiUrl(path), {
    method: "POST",
    headers: baseHeaders(true),
    body: JSON.stringify(body)
  });
  return parseJsonResponse(res, path);
}

async function postJsonPublic(path: string, body: unknown): Promise<Json> {
  const res = await fetch(apiUrl(path), {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  return parseJsonResponse(res, path);
}

async function patchJson(path: string, body: unknown): Promise<Json> {
  const res = await fetch(apiUrl(path), {
    method: "PATCH",
    headers: baseHeaders(true),
    body: JSON.stringify(body)
  });
  return parseJsonResponse(res, path);
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

export type RegisterPayload = {
  nome: string;
  email: string;
  password: string;
  role: "HERO" | "VILLAIN";
};

export const api = {
  login: (email: string, password: string) =>
    postJsonPublic("/api/auth/login", {
      email: email.trim(),
      password
    }) as Promise<LoginResponse>,

  register: (payload: RegisterPayload) =>
    postJsonPublic("/api/auth/register", payload) as Promise<LoginResponse>,

  getAuthMe: () => getJson("/api/auth/me") as Promise<AuthUser>,

  getHello: () => getJson("/api/hello"),
  getHealth: () => getJson("/api/health"),
  postMissionTest: (payload: MissionTestPayload) => postJsonPublic("/api/missions/test", payload),
  postPublishTestEvent: (mensagem?: string) =>
    postJsonPublic("/api/events/publish-test", { mensagem: mensagem ?? null }),

  listMyMissions: () => getJson("/api/me/missions") as Promise<Mission[]>,

  createMission: (payload: CreateMissionPayload) =>
    postJson("/api/player-missions", payload) as Promise<Mission>,
  listMissions: () => getJson("/api/player-missions") as Promise<Mission[]>,
  getMissionDetail: (id: string) => getJson(`/api/player-missions/${id}`) as Promise<MissionDetail>,
  inviteMissionMember: (missionId: string, userId: string) =>
    postJson(`/api/player-missions/${missionId}/members/invite`, { userId }) as Promise<MissionMember>,
  acceptMissionInvite: (missionId: string) =>
    postJson(`/api/player-missions/${missionId}/members/accept`, {}) as Promise<MissionMember>,
  declineMissionInvite: (missionId: string) =>
    postJson(`/api/player-missions/${missionId}/members/decline`, {}) as Promise<MissionMember>,
  listMissionMembers: (missionId: string) =>
    getJson(`/api/player-missions/${missionId}/members`) as Promise<MissionMember[]>,
  startMission: (missionId: string, forceStart = false) =>
    postJson(`/api/player-missions/${missionId}/start`, { forceStart }) as Promise<Mission>,
  createMissionTask: (missionId: string, payload: CreateTaskPayload) =>
    postJson(`/api/player-missions/${missionId}/tasks`, payload) as Promise<MissionTask>,
  updateMissionTask: (missionId: string, taskId: string, payload: CreateTaskPayload) =>
    patchJson(`/api/player-missions/${missionId}/tasks/${taskId}`, payload) as Promise<MissionTask>,
  executeMissionTask: (missionId: string, taskId: string, payload: ExecuteTaskPayload) =>
    postJson(`/api/player-missions/${missionId}/tasks/${taskId}/execute`, payload) as Promise<MissionTask>,
  closeMission: (missionId: string) =>
    postJson(`/api/player-missions/${missionId}/close`, {}) as Promise<void>,

  listHeroes: () => getJson("/api/heroes") as Promise<Hero[]>,
  getHero: (id: string) => getJson(`/api/heroes/${id}`) as Promise<HeroDetail>,
  createHero: (payload: CreateHeroPayload) => postJson("/api/heroes", payload) as Promise<Hero>,
  patchHeroAvailability: (id: string, disponibilidade: HeroiDisponibilidade) =>
    patchJson(`/api/heroes/${id}/availability`, { disponibilidade }) as Promise<Hero>,

  listTeams: () => getJson("/api/teams") as Promise<Equipe[]>,
  getTeam: (id: string) => getJson(`/api/teams/${id}`) as Promise<EquipeDetail>,
  createTeam: (payload: CreateTeamPayload) => postJson("/api/teams", payload) as Promise<Equipe>,

  listVillainTargets: () => getJson("/api/villain/targets") as Promise<Mission[]>,
  startInfiltration: async (missionId: string) =>
    normalizeDuelSession(
      (await postJson(`/api/villain/infiltrate/${missionId}`, {})) as DuelSession
    ),

  getActiveDuelForMission: async (missionId: string): Promise<DuelSession | null> => {
    const res = await fetch(apiUrl(`/api/duels/by-mission/${missionId}`), {
      headers: baseHeaders(false)
    });
    if (res.status === 204 || res.status === 404) return null;
    const json = await parseJsonResponse(res, `/api/duels/by-mission/${missionId}`);
    return json as DuelSession;
  },

  getDuelState: async (duelId: string) => {
    const raw = (await getJson(`/api/duels/${duelId}`)) as DuelSession;
    return normalizeDuelSession(raw);
  },
  joinDuel: async (duelId: string) =>
    normalizeDuelSession((await postJson(`/api/duels/${duelId}/join`, {})) as DuelSession),

  submitInfiltrationProgress: async (duelId: string, payload: PuzzleProgressPayload) =>
    normalizeDuelSession(
      (await postJson(`/api/duels/${duelId}/infiltration-progress`, payload)) as DuelSession
    ),

  submitPuzzleProgress: async (duelId: string, payload: PuzzleProgressPayload) =>
    normalizeDuelSession(
      (await postJson(`/api/duels/${duelId}/progress`, payload)) as DuelSession
    ),
  submitSabotageChoice: (
    duelId: string,
    sabotageType: SabotageType,
    targetScope: SabotageScope,
    targetUserId?: string | null
  ) =>
    postJson(`/api/duels/${duelId}/sabotage-choice`, {
      sabotageType,
      targetScope,
      targetUserId: targetUserId ?? null
    }) as Promise<void>
};
