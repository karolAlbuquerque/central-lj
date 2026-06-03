import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { DuelOutcomeFlash } from "../../components/DuelOutcomeFlash/DuelOutcomeFlash";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { MissionTaskPuzzle } from "../../components/puzzles/MissionTaskPuzzle";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { api } from "../../services/api";
import type { DuelSession, DuelStatus } from "../../types/pvp";
import {
  duelExitPath,
  isDuelFinished,
  resolveDuelOutcome,
  type DuelOutcome
} from "../../utils/duelOutcome";
import { PUZZLE_LABELS } from "../../utils/puzzle";
import styles from "./DuelArenaPage.module.css";

const POLL_STATUSES: DuelStatus[] = ["INFILTRATING", "PENDING", "ACTIVE"];
const DUEL_PUZZLES = 3;
const OUTCOME_REDIRECT_MS = 3400;

export function DuelArenaPage() {
  const { duelId } = useParams<{ duelId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [duel, setDuel] = useState<DuelSession | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [outcome, setOutcome] = useState<DuelOutcome | null>(null);
  const [outcomePulse, setOutcomePulse] = useState(0);
  const startTime = useRef<number>(Date.now());
  const duelStatusRef = useRef<DuelStatus | null>(null);
  const outcomeHandledRef = useRef(false);
  const redirectTimerRef = useRef<number | null>(null);

  const clearRedirectTimer = () => {
    if (redirectTimerRef.current) {
      window.clearTimeout(redirectTimerRef.current);
      redirectTimerRef.current = null;
    }
  };

  const scheduleExitAfterOutcome = useCallback(
    (finishedDuel: DuelSession, resolved: DuelOutcome) => {
      if (outcomeHandledRef.current) return;
      outcomeHandledRef.current = true;
      setOutcome(resolved);
      setOutcomePulse(p => p + 1);
      clearRedirectTimer();
      redirectTimerRef.current = window.setTimeout(() => {
        const path = duelExitPath(finishedDuel, user?.role);
        navigate(path, { replace: true });
      }, OUTCOME_REDIRECT_MS);
    },
    [navigate, user?.role]
  );

  const maybeHandleFinished = useCallback(
    (d: DuelSession) => {
      if (!isDuelFinished(d.status) || outcomeHandledRef.current) return;
      const resolved = resolveDuelOutcome(d, user?.id, user?.role);
      if (!resolved) return;
      scheduleExitAfterOutcome(d, resolved);
    },
    [scheduleExitAfterOutcome, user?.id, user?.role]
  );

  const load = useCallback(
    async (silent = false) => {
      if (!duelId) return;
      if (!silent) {
        setLoading(true);
      }
      setErr(null);
      try {
        const d = await api.getDuelState(duelId);
        const prev = duelStatusRef.current;
        duelStatusRef.current = d.status;
        setDuel(d);
        if (d.status === "ACTIVE" && (prev !== "ACTIVE" || silent)) {
          startTime.current = Date.now();
        }
        maybeHandleFinished(d);
      } catch (e) {
        if (!silent) {
          setErr(e instanceof Error ? e.message : "Falha ao carregar duelo.");
        }
      } finally {
        if (!silent) {
          setLoading(false);
        }
      }
    },
    [duelId, maybeHandleFinished]
  );

  useEffect(() => {
    outcomeHandledRef.current = false;
    setOutcome(null);
    duelStatusRef.current = null;
    clearRedirectTimer();
  }, [duelId]);

  useEffect(() => {
    void load(false);
    return () => clearRedirectTimer();
  }, [load]);

  useEffect(() => {
    if (!duelId || !duel) return undefined;
    if (!POLL_STATUSES.includes(duel.status)) return undefined;
    const t = window.setInterval(() => {
      void load(true);
    }, 2500);
    return () => window.clearInterval(t);
  }, [duelId, duel?.status, load]);

  const handleInfiltrationSubmit = async (moves: number[]) => {
    if (!duelId || !duel) return;
    setSubmitting(true);
    setErr(null);
    try {
      const progress = duel.infiltrationProgress ?? 0;
      const result = await api.submitInfiltrationProgress(duelId, {
        moves,
        roundNumber: progress + 1,
        timeMs: Date.now() - startTime.current
      });
      duelStatusRef.current = result.status;
      setDuel(result);
      startTime.current = Date.now();
      maybeHandleFinished(result);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha na brecha de infiltração.");
      if (e instanceof Error && e.message.includes("falhou")) {
        void load(true);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleAcceptDuel = async () => {
    if (!duelId) return;
    setSubmitting(true);
    setErr(null);
    try {
      const result = await api.joinDuel(duelId);
      duelStatusRef.current = result.status;
      setDuel(result);
      startTime.current = Date.now();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao aceitar o duelo.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDuelSubmit = async (moves: number[]) => {
    if (!duelId || !duel || !user) return;
    const myCompleted =
      user.id === duel.attackerUserId ? duel.attackerRoundsWon : duel.defenderRoundsWon;
    setSubmitting(true);
    setErr(null);
    try {
      const result = await api.submitPuzzleProgress(duelId, {
        moves,
        roundNumber: myCompleted + 1,
        timeMs: Date.now() - startTime.current
      });
      duelStatusRef.current = result.status;
      setDuel(result);
      startTime.current = Date.now();
      maybeHandleFinished(result);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao enviar resposta.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading && !duel) return <LoadingState message="Carregando arena…" />;
  if (!duel) return <EmptyState title="Duelo não encontrado" />;

  const isAttacker = user?.id === duel.attackerUserId;
  const isDefender = user?.id === duel.defenderUserId;
  const infiltrationRequired = duel.infiltrationRequired ?? 3;
  const infiltrationProgress = duel.infiltrationProgress ?? 0;
  const isFinished = isDuelFinished(duel.status);
  const infiltrationStep = Math.min(infiltrationProgress + 1, infiltrationRequired);
  const puzzlesToWin = duel.roundMax ?? DUEL_PUZZLES;
  const myCompleted = isAttacker ? duel.attackerRoundsWon : isDefender ? duel.defenderRoundsWon : 0;
  const myPuzzleStep = Math.min(myCompleted + 1, puzzlesToWin);
  const canFight = isAttacker || isDefender;

  const outcomeOverlay =
    outcome ? (
      <DuelOutcomeFlash
        kind={outcome.kind}
        headline={outcome.headline}
        subline={outcome.subline}
        pulse={outcomePulse}
      />
    ) : null;

  if (duel.status === "INFILTRATING") {
    if (isAttacker) {
      return (
        <div className={styles.page}>
          {outcomeOverlay}
          <PageHeader
            kicker="Fase de infiltração"
            title={`Brecha ${infiltrationStep} de ${infiltrationRequired}`}
            description="Complete os 3 puzzles de brecha. Depois o combate com o herói começa na hora."
          />
          {err ? <p className={styles.error}>{err}</p> : null}
          <div className={styles.infiltrationBar}>
            {Array.from({ length: infiltrationRequired }).map((_, i) => (
              <span
                key={i}
                className={`${styles.infiltrationDot} ${
                  i < infiltrationProgress
                    ? styles.infiltrationDotDone
                    : i === infiltrationProgress
                      ? styles.infiltrationDotActive
                      : ""
                }`}
              />
            ))}
          </div>
          <SectionCard title={`Minigame: ${PUZZLE_LABELS[duel.puzzleType]}`}>
            <MissionTaskPuzzle
              key={`${duel.seed}-${infiltrationProgress}`}
              puzzleType={duel.puzzleType}
              seed={duel.seed}
              busy={submitting}
              onMistake={(flash) => setErr(`Resposta incorreta — ${flash}. Verifique e tente novamente.`)}
              onSubmit={(moves) => {
                void handleInfiltrationSubmit(moves);
              }}
            />
          </SectionCard>
        </div>
      );
    }
    return (
      <div className={styles.page}>
        {outcomeOverlay}
        <EmptyState
          title="Infiltração em andamento"
          hint="Um vilão está rompendo as defesas. O alerta só aparece quando as 3 brechas forem concluídas."
        />
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {outcomeOverlay}
      <PageHeader
        kicker="Arena de duelo"
        title={
          isDefender
            ? "Sua missão foi invadida"
            : isAttacker
              ? "Combate com o herói"
              : "Duelo em andamento"
        }
        description={
          isDefender
            ? "Derrote o vilão — resolva 3 puzzles antes que ele termine os dele."
            : isAttacker
              ? "Derrote o herói — quem completar 3 puzzles primeiro vence."
              : "Corrida de puzzles entre vilão e herói."
        }
      />

      {err ? <p className={styles.error}>{err}</p> : null}

      <div className={styles.scoreRow}>
        <div className={styles.scoreBlock}>
          <span className={styles.scoreLabel}>Vilão</span>
          <span className={styles.scoreValue}>
            {duel.attackerRoundsWon}
            <span className={styles.scoreOf}> / {puzzlesToWin}</span>
          </span>
        </div>
        <span className={styles.vs}>VS</span>
        <div className={styles.scoreBlock}>
          <span className={styles.scoreLabel}>Herói</span>
          <span className={styles.scoreValue}>
            {duel.defenderRoundsWon}
            <span className={styles.scoreOf}> / {puzzlesToWin}</span>
          </span>
        </div>
      </div>

      {duel.status === "PENDING" && canFight ? (
        <SectionCard title="Aguardando aceite do herói" variant="alert">
          {isDefender ? (
            <>
              <p className={styles.waiting}>
                O vilão concluiu as 3 brechas. Aceite o duelo para iniciar o combate de puzzles — se
                recusar ou demorar, a missão pode ser comprometida.
              </p>
              <button
                type="button"
                className={styles.btnAcceptDuel}
                disabled={submitting}
                onClick={() => {
                  void handleAcceptDuel();
                }}
              >
                {submitting ? "Aceitando…" : "Aceitar duelo e iniciar combate"}
              </button>
            </>
          ) : (
            <p className={styles.waiting}>
              Brechas concluídas. Aguardando o herói aceitar o duelo na arena para o combate começar…
            </p>
          )}
        </SectionCard>
      ) : isFinished && outcome ? (
        <SectionCard title="Duelo encerrado" variant="alert">
          <p className={styles.winnerMsg}>{outcome.headline}</p>
          {outcome.subline ? <p className={styles.waiting}>{outcome.subline}</p> : null}
        </SectionCard>
      ) : duel.status === "ACTIVE" && canFight && myCompleted < puzzlesToWin ? (
        <SectionCard title={`Puzzle ${myPuzzleStep} de ${puzzlesToWin} · ${PUZZLE_LABELS[duel.puzzleType]}`}>
          <MissionTaskPuzzle
            key={`${duel.seed}-${myPuzzleStep}`}
            puzzleType={duel.puzzleType}
            seed={duel.seed}
            busy={submitting}
            onMistake={(flash) => setErr(`Resposta incorreta — ${flash}. Verifique e tente novamente.`)}
            onSubmit={(moves) => {
              void handleDuelSubmit(moves);
            }}
          />
        </SectionCard>
      ) : duel.status === "ACTIVE" && canFight ? (
        <SectionCard title="Aguardando resultado">
          <p className={styles.waiting}>Você concluiu seus puzzles. Aguardando o adversário…</p>
        </SectionCard>
      ) : isFinished ? (
        <SectionCard title="Encerrado">
          <p className={styles.waiting}>Processando resultado do duelo…</p>
        </SectionCard>
      ) : (
        <SectionCard title="Aguardando">
          <p className={styles.waiting}>Sincronizando estado do duelo…</p>
        </SectionCard>
      )}
    </div>
  );
}
