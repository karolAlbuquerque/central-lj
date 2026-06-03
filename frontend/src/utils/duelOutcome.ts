import type { DuelSession } from "../types/pvp";

export type DuelOutcomeKind = "win" | "lose" | "cancelled";

export type DuelOutcome = {
  kind: DuelOutcomeKind;
  headline: string;
  subline?: string;
};

const FINISHED: DuelSession["status"][] = ["HERO_WON", "VILLAIN_WON", "TIMEOUT", "CANCELLED"];

export function isDuelFinished(status: DuelSession["status"]): boolean {
  return FINISHED.includes(status);
}

function heroWon(status: DuelSession["status"], villainAhead: boolean): boolean {
  if (status === "HERO_WON") return true;
  if (status === "VILLAIN_WON") return false;
  if (status === "TIMEOUT") return !villainAhead;
  return false;
}

export function resolveDuelOutcome(
  duel: DuelSession,
  userId: string | undefined,
  userRole: string | undefined
): DuelOutcome | null {
  if (!userId || !isDuelFinished(duel.status)) return null;

  const isAttacker = userId === duel.attackerUserId;
  const isDefender = userId === duel.defenderUserId;
  if (!isAttacker && !isDefender) return null;

  const villainName = duel.attackerName?.trim() || "o vilão";
  const heroName = duel.defenderName?.trim() || "o herói";
  const opponentName = isAttacker ? heroName : villainName;

  if (duel.status === "CANCELLED") {
    return {
      kind: "cancelled",
      headline: "DUELO CANCELADO",
      subline: "A infiltração ou o combate foi encerrado antes do fim."
    };
  }

  const villainAhead = duel.attackerRoundsWon > duel.defenderRoundsWon;
  const heroVictory = heroWon(duel.status, villainAhead);
  const iWon = isAttacker ? !heroVictory : heroVictory;

  if (iWon) {
    return {
      kind: "win",
      headline: `VOCÊ DERROTOU ${opponentName.toUpperCase()}!`,
      subline:
        userRole === "VILLAIN"
          ? "A missão foi derrotada. Retornando às operações…"
          : "A missão continua. Voltando às tarefas…"
    };
  }

  return {
    kind: "lose",
    headline: `VOCÊ PERDEU PARA ${opponentName.toUpperCase()}!`,
    subline:
      userRole === "VILLAIN"
        ? "Recuando para a sala de operações…"
        : "A missão foi marcada como derrotada. Voltando às tarefas…"
  };
}

export function duelExitPath(duel: DuelSession, userRole: string | undefined): string {
  if (userRole === "VILLAIN") {
    return "/vilao/ops";
  }
  return `/missoes/${duel.missionId}/tarefas`;
}
