import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { MissionTaskPuzzle } from "../../components/puzzles/MissionTaskPuzzle";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { api } from "../../services/api";
import type { DuelSession, DuelStatus } from "../../types/pvp";
import { PUZZLE_LABELS } from "../../utils/puzzle";
import styles from "./DuelArenaPage.module.css";

const POLL_STATUSES: DuelStatus[] = ["INFILTRATING", "PENDING", "ACTIVE"];

export function DuelArenaPage() {
  const { duelId } = useParams<{ duelId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [duel, setDuel] = useState<DuelSession | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const startTime = useRef<number>(Date.now());
  const duelStatusRef = useRef<DuelStatus | null>(null);

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
    [duelId]
  );

  useEffect(() => {
    duelStatusRef.current = null;
    void load(false);
  }, [load]);

  useEffect(() => {
    if (!duelId || !duel) return undefined;
    if (!POLL_STATUSES.includes(duel.status)) return undefined;
    const t = window.setInterval(() => {
      void load(true);
    }, 2500);
    return () => window.clearInterval(t);
  }, [duelId, duel?.status, load]);

  const handleJoin = async () => {
    if (!duelId) return;
    setSubmitting(true);
    setErr(null);
    try {
      const joined = await api.joinDuel(duelId);
      duelStatusRef.current = joined.status;
      setDuel(joined);
      startTime.current = Date.now();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao entrar no duelo.");
    } finally {
      setSubmitting(false);
    }
  };

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
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha na brecha de infiltração.");
      if (e instanceof Error && e.message.includes("falhou")) {
        void load(true);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDuelSubmit = async (moves: number[]) => {
    if (!duelId || !duel) return;
    setSubmitting(true);
    setErr(null);
    try {
      const result = await api.submitPuzzleProgress(duelId, {
        moves,
        roundNumber: duel.roundCurrent,
        timeMs: Date.now() - startTime.current
      });
      duelStatusRef.current = result.status;
      setDuel(result);
      startTime.current = Date.now();
      if (result.status === "VILLAIN_WON" && user?.role === "VILLAIN") {
        navigate(`/vilao/duelo/${duelId}/sabotagem`);
      }
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
  const isFinished = ["HERO_WON", "VILLAIN_WON", "CANCELLED", "TIMEOUT"].includes(duel.status);
  const winner =
    duel.status === "HERO_WON"
      ? "Herói venceu!"
      : duel.status === "VILLAIN_WON"
        ? "Vilão venceu!"
        : null;

  const infiltrationStep = Math.min(infiltrationProgress + 1, infiltrationRequired);

  if (duel.status === "INFILTRATING") {
    if (isAttacker) {
      return (
        <div className={styles.page}>
          <PageHeader
            kicker="Fase de infiltração"
            title={`Brecha ${infiltrationStep} de ${infiltrationRequired}`}
            description="Complete os 3 puzzles para romper as defesas. Só depois a equipe inimiga será alertada."
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
        <EmptyState
          title="Infiltração em andamento"
          hint="Um vilão está rompendo as defesas. O alerta só aparece quando as 3 brechas forem concluídas."
        />
      </div>
    );
  }

  if (duel.status === "PENDING") {
    return (
      <div className={styles.page}>
        <PageHeader
          kicker="Duelo"
          title="Defesas rompidas"
          description={
            isAttacker
              ? "Brecha concluída. Aguarde o defensor entrar na arena — os puzzles de combate começam em seguida."
              : "Um vilão infiltrou a missão. Entre na arena para iniciar o combate de puzzles."
          }
        />
        {err ? <p className={styles.error}>{err}</p> : null}
        <SectionCard title="Aguardando combate">
          {isDefender ? (
            <button
              type="button"
              className={styles.btnSubmit}
              disabled={submitting}
              onClick={() => {
                void handleJoin();
              }}
            >
              {submitting ? "Entrando…" : "Aceitar duelo e entrar na arena"}
            </button>
          ) : (
            <p className={styles.waiting}>
              Aguardando o herói defensor aceitar o duelo… A tela atualiza automaticamente quando o
              combate começar.
            </p>
          )}
          <Link className={styles.missionLink} to={`/missoes/${duel.missionId}`}>
            Voltar à missão
          </Link>
        </SectionCard>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Arena de duelo"
        title={`Round ${duel.roundCurrent} / ${duel.roundMax}`}
        description={`Status: ${duel.status} · Puzzle: ${duel.puzzleType}`}
      />

      {err ? <p className={styles.error}>{err}</p> : null}

      <div className={styles.scoreRow}>
        <div className={styles.scoreBlock}>
          <span className={styles.scoreLabel}>Herói</span>
          <span className={styles.scoreValue}>{duel.defenderRoundsWon}</span>
        </div>
        <span className={styles.vs}>VS</span>
        <div className={styles.scoreBlock}>
          <span className={styles.scoreLabel}>Vilão</span>
          <span className={styles.scoreValue}>{duel.attackerRoundsWon}</span>
        </div>
      </div>

      {winner ? (
        <SectionCard title="Duelo encerrado" variant="alert">
          <p className={styles.winnerMsg}>{winner}</p>
        </SectionCard>
      ) : duel.status === "ACTIVE" ? (
        <SectionCard title={`Combate: ${PUZZLE_LABELS[duel.puzzleType]}`}>
          <p className={styles.waiting}>
            {isAttacker ? "Derrote o defensor neste minigame." : "Defenda a missão neste minigame."}
          </p>
          <MissionTaskPuzzle
            key={`${duel.seed}-${duel.roundCurrent}`}
            puzzleType={duel.puzzleType}
            seed={duel.seed}
            busy={submitting}
            onSubmit={(moves) => {
              void handleDuelSubmit(moves);
            }}
          />
        </SectionCard>
      ) : isFinished ? (
        <SectionCard title="Encerrado">
          <p className={styles.waiting}>Este duelo foi encerrado.</p>
        </SectionCard>
      ) : (
        <SectionCard title="Aguardando">
          <p className={styles.waiting}>Sincronizando estado do duelo…</p>
        </SectionCard>
      )}
    </div>
  );
}
