import { useCallback, useEffect, useRef, useState } from "react";
import { api } from "../services/api";
import type { MissionCombatState } from "../types/pvp";

const ALERT_STATES: MissionCombatState[] = ["ALERTA_INFILTRACAO", "EM_DUELO"];

function wasAlertState(state: MissionCombatState | null): boolean {
  return state !== null && ALERT_STATES.includes(state);
}

export function useMissionInfiltrationAlert(missionId: string | undefined) {
  const [showAlert, setShowAlert] = useState(false);
  const [duelId, setDuelId] = useState<string | null>(null);
  const lastState = useRef<MissionCombatState | null>(null);
  const dismissed = useRef(false);

  useEffect(() => {
    const mid = missionId;
    if (!mid) return undefined;
    let cancelled = false;

    async function poll(mId: string) {
      try {
        const detail = await api.getMissionDetail(mId);
        const state = detail.mission.combatState;
        const prev = lastState.current;
        lastState.current = state;

        if (!ALERT_STATES.includes(state)) {
          dismissed.current = false;
          return;
        }

        const duel = await api.getActiveDuelForMission(mId).catch(() => null);
        if (cancelled) return;
        setDuelId(duel?.id ?? null);

        const justEntered = wasAlertState(state) && !wasAlertState(prev);
        const openOnLoad =
          prev === null && (state === "EM_DUELO" || state === "ALERTA_INFILTRACAO");
        if ((justEntered || openOnLoad) && !dismissed.current) {
          setShowAlert(true);
        }
      } catch {
        /* polling silencioso */
      }
    }

    lastState.current = null;
    void poll(mid);
    const t = window.setInterval(() => {
      void poll(mid);
    }, 4000);
    return () => {
      cancelled = true;
      window.clearInterval(t);
    };
  }, [missionId]);

  const dismiss = useCallback(() => {
    setShowAlert(false);
    dismissed.current = true;
  }, []);

  return { showAlert, duelId, dismiss };
}
