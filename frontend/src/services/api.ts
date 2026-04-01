import { apiUrl } from "../config/api";
import type {
  CreateMissionPayload,
  DashboardSummary,
  Mission,
  MissionDetail,
  MissionTestPayload
} from "../types/mission";

type Json = unknown;

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
  const res = await fetch(apiUrl(path), { headers: { Accept: "application/json" } });
  return parseJsonResponse(res);
}

async function postJson(path: string, body: unknown): Promise<Json> {
  const res = await fetch(apiUrl(path), {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  return parseJsonResponse(res);
}

export const api = {
  getHello: () => getJson("/api/hello"),
  getHealth: () => getJson("/api/health"),
  postMissionTest: (payload: MissionTestPayload) => postJson("/api/missions/test", payload),
  postPublishTestEvent: (mensagem?: string) =>
    postJson("/api/events/publish-test", { mensagem: mensagem ?? null }),

  listMissions: () => getJson("/api/missions") as Promise<Mission[]>,
  getDashboardSummary: () => getJson("/api/missions/dashboard/summary") as Promise<DashboardSummary>,
  getMissionDetail: (id: string) => getJson(`/api/missions/${id}`) as Promise<MissionDetail>,
  createMission: (payload: CreateMissionPayload) => postJson("/api/missions", payload) as Promise<Mission>
};
