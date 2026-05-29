import { useCallback, useEffect, useRef, useState, type CSSProperties } from "react";
import type { PuzzleType } from "../../types/pvp";
import { Link, useLocation, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { PuzzleModal } from "../../components/puzzles/PuzzleModal";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { MissionTaskPuzzle } from "../../components/puzzles/MissionTaskPuzzle";
import { api } from "../../services/api";
import type { MissionDetail, MissionTask } from "../../types/pvp";
import { MissionInfiltrationAlert } from "../../components/MissionInfiltrationAlert/MissionInfiltrationAlert";
import { useMissionInfiltrationAlert } from "../../hooks/useMissionInfiltrationAlert";
import { PUZZLE_LABELS } from "../../utils/puzzle";
import styles from "./MissionTasksPage.module.css";

const PUZZLE_TIME_LIMIT_SEC = 75;
const PUZZLE_MAX_ERRORS = 3;
const PUZZLE_FAIL_CLOSE_MS = 3200;

function mistakeDetailFor(puzzleType: PuzzleType | null | undefined, flash: string): string {
  if (puzzleType === "CARD_MATCH") return "Você errou um par de cartas.";
  if (puzzleType === "NODE_CONNECT") return "Os fios foram conectados na ordem errada.";
  if (puzzleType === "SEQUENCE_INPUT") return "A sequência digitada não confere.";
  if (puzzleType === "DRAG_SORT") return "A ordem dos itens está incorreta.";
  if (puzzleType === "TILE_ROTATE") return "As peças de rotação não ficaram alinhadas.";
  if (puzzleType === "COLOR_SIMON") return "A sequência de cores foi digitada errada.";
  if (puzzleType === "TERMINAL_HACK") return "Os tokens de terminal não conferem.";
  if (puzzleType === "SLIDING_TILE") return "O estado final do sliding tile está incorreto.";
  if (puzzleType === "ARCADE_SHOOTER") return "A sequência tática de tiro falhou.";
  if (puzzleType === "ARCADE_DODGE") return "A rota de esquiva foi comprometida.";
  if (flash.includes("BOOOM")) return "Erro ao desarmar os fios.";
  return "Erro no minigame.";
}

function limitFlashText(puzzleType: PuzzleType | null | undefined, lastFlash: string): string {
  if (puzzleType === "NODE_CONNECT") return "BOOOM! — LIMITE DE ERROS";
  if (puzzleType === "CARD_MATCH") return "PARES QUEBRADOS! — LIMITE";
  if (puzzleType === "SEQUENCE_INPUT") return "MEMÓRIA FALHOU! — LIMITE";
  if (puzzleType === "DRAG_SORT") return "SEQUÊNCIA ROMPIDA! — LIMITE";
  if (puzzleType === "TILE_ROTATE") return "ROTAÇÃO QUEBRADA! — LIMITE";
  if (puzzleType === "COLOR_SIMON") return "SEQUÊNCIA CROMÁTICA QUEBRADA! — LIMITE";
  if (puzzleType === "TERMINAL_HACK") return "TERMINAL BLOQUEADO! — LIMITE";
  if (puzzleType === "SLIDING_TILE") return "GRADE TRAVADA! — LIMITE";
  if (puzzleType === "ARCADE_SHOOTER") return "ARCADE OFFLINE! — LIMITE";
  if (puzzleType === "ARCADE_DODGE") return "ESQUIVA ROMPIDA! — LIMITE";
  return `${lastFlash} — LIMITE DE ERROS`;
}

function limitPageMessage(
  puzzleType: PuzzleType | null | undefined,
  detail: string,
  lastFlash: string
): string {
  const name = puzzleType ? PUZZLE_LABELS[puzzleType] : "minigame";
  return `${detail} Você cometeu ${PUZZLE_MAX_ERRORS} erros em «${name}» (${lastFlash}). O puzzle foi encerrado — tente novamente pela tarefa.`;
}

const STATUS_LABEL: Record<MissionTask["status"], string> = {
  PENDING: "Pendente",
  IN_PROGRESS: "Em andamento",
  AWAITING_PUZZLE: "Aguardando puzzle",
  DONE: "Concluída",
  BLOCKED: "Bloqueada"
};

export function MissionTasksPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const { user } = useAuth();
  const [detail, setDetail] = useState<MissionDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [introVisible, setIntroVisible] = useState(false);
  const { showAlert, duelId: alertDuelId, dismiss: dismissAlert } = useMissionInfiltrationAlert(id);

  const load = useCallback(async (silent = false) => {
    if (!id) return;
    if (!silent) setLoading(true);
    setErr(null);
    try {
      setDetail(await api.getMissionDetail(id));
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar tarefas.");
    } finally {
      if (!silent) setLoading(false);
    }
  }, [id]);

  useEffect(() => { void load(); }, [load]);

  useEffect(() => {
    const state = location.state as { playIntro?: boolean } | null;
    if (!state?.playIntro) return;
    setIntroVisible(true);
    const t = window.setTimeout(() => setIntroVisible(false), 5200);
    return () => window.clearTimeout(t);
  }, [location.state]);

  if (loading) return <LoadingState message="Carregando tarefas…" />;
  if (!detail || !id) return <EmptyState title="Missão não encontrada" />;

  const { mission, members, tasks } = detail;
  const isMissionCreator = mission.ownerUserId === user?.id;
  const myTasks = tasks.filter(t => t.canExecute);
  const otherTasks = tasks.filter(t => !t.canExecute);

  const ownerName =
    members.find(m => m.userId === mission.ownerUserId)?.userName ??
    (user?.id === mission.ownerUserId ? user.nome : `${mission.ownerUserId.slice(0, 8)}…`);

  const doneTasks = tasks.filter(t => t.status === "DONE").length;
  const progress = tasks.length > 0 ? Math.round((doneTasks / tasks.length) * 100) : 0;

  const partyMembers = members.filter((m) => m.inviteStatus !== "DECLINED");

  return (
    <div className={styles.page}>
      {showAlert && id ? (
        <MissionInfiltrationAlert missionId={id} duelId={alertDuelId} onDismiss={dismissAlert} />
      ) : null}
      {introVisible ? (
        <div className={styles.partyIntro} aria-hidden>
          <div className={styles.partyIntroStreaks}>
            {partyMembers.map((m, idx) => (
              <span
                key={m.id}
                className={styles.partyStreak}
                style={
                  {
                    ["--intro-delay" as string]: `${idx * 260}ms`,
                    ["--intro-color" as string]: `hsl(${(idx * 67) % 360} 90% 58%)`
                  } as CSSProperties
                }
              />
            ))}
          </div>
          <div className={styles.partyIntroNames}>
            {partyMembers.map((m, idx) => (
              <div
                key={`${m.id}-name`}
                className={styles.partyIntroName}
                style={{ animationDelay: `${idx * 320 + 280}ms` }}
              >
                {m.userName}
              </div>
            ))}
          </div>
          <div className={styles.partyIntroFinal}>PARTY READY</div>
        </div>
      ) : null}
      <PageHeader
        kicker="Missão"
        title={mission.titulo}
        description={
          isMissionCreator
            ? "Execute tarefas críticas no minigame definido pelo chefe."
            : "Execute as tarefas atribuídas a você."
        }
        actions={
          <Link className={styles.btnLink} to={`/missoes/${id}`}>
            Sala de comando
          </Link>
        }
      />

      {/* Mission context strip */}
      <div className={styles.missionContext}>
        <div className={styles.contextMeta}>
          <div className={styles.metaGroup}>
            <span className={styles.metaLabel}>Criado por</span>
            <span className={styles.metaValue}>{ownerName}</span>
          </div>
          <span className={styles.metaDot} aria-hidden>·</span>
          <div className={styles.metaGroup}>
            <span className={styles.metaLabel}>Equipe</span>
            <span className={styles.metaValue}>{members.length} participante{members.length !== 1 ? "s" : ""}</span>
          </div>
          <span className={styles.metaDot} aria-hidden>·</span>
          <div className={styles.metaGroup}>
            <span className={styles.metaLabel}>Tarefas</span>
            <span className={styles.metaValue}>{doneTasks}/{tasks.length} concluída{doneTasks !== 1 ? "s" : ""}</span>
          </div>
        </div>
        {tasks.length > 0 ? (
          <div className={styles.progressWrap}>
            <div className={styles.progressBar}>
              <div className={styles.progressFill} style={{ width: `${progress}%` }} />
            </div>
            <span className={styles.progressLabel}>{progress}%</span>
          </div>
        ) : null}
      </div>

      {err ? <p className={styles.error}>{err}</p> : null}

      <SectionCard
        title={isMissionCreator ? "Tarefas que você pode executar" : "Minhas tarefas"}
        hint="Pendente → Iniciar → Concluir (críticas exigem minigame)"
      >
        {myTasks.length === 0 ? (
          <EmptyState
            title="Nenhuma tarefa disponível"
            hint={
              isMissionCreator
                ? "Crie tarefas na sala de comando (marque crítica para minigame)."
                : "O chefe ainda não atribuiu tarefas a você."
            }
          />
        ) : (
          <ul className={styles.taskList}>
            {myTasks.map(t => (
              <TaskItem
                key={t.id}
                missionId={id}
                task={t}
                onUpdated={() => { void load(true); }}
                onError={setErr}
              />
            ))}
          </ul>
        )}
      </SectionCard>

      {otherTasks.length > 0 ? (
        <SectionCard
          title="Outras tarefas"
          hint={`${otherTasks.length} tarefa${otherTasks.length !== 1 ? "s" : ""} de outros membros`}
        >
          <div className={styles.otherGrid}>
            {otherTasks.map(t => (
              <div
                key={t.id}
                className={[styles.otherCard, styles[`s_${t.status}` as keyof typeof styles]].filter(Boolean).join(" ")}
              >
                <span className={styles.taskTitle}>{t.title}</span>
                <div className={styles.badges}>
                  {t.critical ? <span className={styles.criticalBadge}>Crítica</span> : null}
                  <span className={`${styles.statusBadge} ${styles[`s_${t.status}` as keyof typeof styles] ?? ""}`}>
                    {STATUS_LABEL[t.status]}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </SectionCard>
      ) : null}
    </div>
  );
}

function TaskItem({
  missionId,
  task,
  onUpdated,
  onError
}: {
  missionId: string;
  task: MissionTask;
  onUpdated: () => void;
  onError: (msg: string | null) => void;
}) {
  const [busy, setBusy] = useState(false);
  const [puzzleOpen, setPuzzleOpen] = useState(false);
  const [timeLeft, setTimeLeft] = useState(PUZZLE_TIME_LIMIT_SEC);
  const [errors, setErrors] = useState(0);
  const [dangerPulse, setDangerPulse] = useState(0);
  const [dangerText, setDangerText] = useState("ERRO!");
  const [gameOverMessage, setGameOverMessage] = useState<string | null>(null);
  const [failureTier, setFailureTier] = useState<"none" | "hint" | "fatal">("none");
  const [puzzleLocked, setPuzzleLocked] = useState(false);
  const failCloseTimerRef = useRef<number | null>(null);
  const puzzleFailedRef = useRef(false);

  const puzzleType = task.puzzleType;
  const canPlayPuzzle = task.critical && task.puzzleSeed && puzzleType;

  const dangerLabelByPuzzle = (fallback = "ERRO!") => {
    if (!puzzleType) return fallback;
    if (puzzleType === "NODE_CONNECT") return "BOOOM!";
    if (puzzleType === "CARD_MATCH") return "PARES QUEBRADOS!";
    if (puzzleType === "SEQUENCE_INPUT") return "MEMÓRIA FALHOU!";
    if (puzzleType === "DRAG_SORT") return "SEQUÊNCIA ROMPIDA!";
    if (puzzleType === "TILE_ROTATE") return "ROTAÇÃO QUEBRADA!";
    if (puzzleType === "COLOR_SIMON") return "SEQUÊNCIA CROMÁTICA QUEBRADA!";
    if (puzzleType === "TERMINAL_HACK") return "TERMINAL BLOQUEADO!";
    if (puzzleType === "SLIDING_TILE") return "GRADE TRAVADA!";
    if (puzzleType === "ARCADE_SHOOTER") return "ARCADE OFFLINE!";
    if (puzzleType === "ARCADE_DODGE") return "ESQUIVA ROMPIDA!";
    return fallback;
  };

  const clearFailCloseTimer = () => {
    if (failCloseTimerRef.current) {
      window.clearTimeout(failCloseTimerRef.current);
      failCloseTimerRef.current = null;
    }
  };

  const showDangerFlash = (flashText: string, tier: "hint" | "fatal" = "hint") => {
    setFailureTier(tier);
    setDangerPulse(p => p + 1);
    setDangerText(flashText);
  };

  const failPuzzle = (flashText: string, pageMessage: string) => {
    if (puzzleFailedRef.current) return;
    puzzleFailedRef.current = true;
    setPuzzleLocked(true);
    setGameOverMessage(pageMessage);
    showDangerFlash(flashText, "fatal");
    onError(pageMessage);
    clearFailCloseTimer();
    failCloseTimerRef.current = window.setTimeout(() => {
      setPuzzleOpen(false);
      setGameOverMessage(null);
      setFailureTier("none");
      setPuzzleLocked(false);
      puzzleFailedRef.current = false;
      failCloseTimerRef.current = null;
    }, PUZZLE_FAIL_CLOSE_MS);
  };

  const registerMistake = (detail: string, flashText?: string) => {
    const flash = flashText ?? dangerLabelByPuzzle();
    setErrors(prev => {
      const next = prev + 1;
      if (next >= PUZZLE_MAX_ERRORS) {
        failPuzzle(limitFlashText(puzzleType, flash), limitPageMessage(puzzleType, detail, flash));
      } else {
        showDangerFlash(flash);
        onError(`${detail} Erro ${next} de ${PUZZLE_MAX_ERRORS}. Restam ${PUZZLE_MAX_ERRORS - next} antes de encerrar o minigame.`);
      }
      return next;
    });
  };

  const run = async (action: "START" | "COMPLETE" | "SUBMIT_PUZZLE", moves?: number[]) => {
    setBusy(true);
    onError(null);
    try {
      const updated = await api.executeMissionTask(missionId, task.id, { action, moves });
      if (action === "COMPLETE" && updated.status === "AWAITING_PUZZLE" && updated.puzzleType) {
        setPuzzleOpen(true);
        setTimeLeft(PUZZLE_TIME_LIMIT_SEC);
        setErrors(0);
        setDangerPulse(0);
      }
      onUpdated();
    } catch (e) {
      onError(e instanceof Error ? e.message : "Falha na operação.");
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    if (!puzzleOpen) return undefined;
    setTimeLeft(PUZZLE_TIME_LIMIT_SEC);
    setErrors(0);
    setGameOverMessage(null);
    setFailureTier("none");
    setPuzzleLocked(false);
    puzzleFailedRef.current = false;
    const interval = window.setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          window.clearInterval(interval);
          const name = puzzleType ? PUZZLE_LABELS[puzzleType] : "minigame";
          failPuzzle(
            "TEMPO ESGOTADO!",
            `O tempo de ${PUZZLE_TIME_LIMIT_SEC}s acabou no minigame «${name}». O puzzle foi encerrado — inicie novamente pela tarefa.`
          );
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => {
      window.clearInterval(interval);
      clearFailCloseTimer();
    };
  }, [puzzleOpen, puzzleType]);

  const submitPuzzle = async (moves: number[]) => {
    setBusy(true);
    onError(null);
    try {
      await api.executeMissionTask(missionId, task.id, { action: "SUBMIT_PUZZLE", moves });
      setPuzzleOpen(false);
      onUpdated();
    } catch (e) {
      const base = e instanceof Error ? e.message : "Solução incorreta.";
      registerMistake(`Envio rejeitado: ${base}`, "SOLUÇÃO INVÁLIDA!");
    } finally {
      setBusy(false);
    }
  };

  const handlePuzzleMistakeWithMessage = (customText: string) => {
    registerMistake(mistakeDetailFor(puzzleType, customText), customText);
  };

  const openPuzzle = () => {
    if (!canPlayPuzzle) {
      onError("Minigame indisponível para esta tarefa. Recarregue a página ou recrie a tarefa.");
      return;
    }
    clearFailCloseTimer();
    puzzleFailedRef.current = false;
    setGameOverMessage(null);
    setFailureTier("none");
    setPuzzleLocked(false);
    setPuzzleOpen(true);
    setTimeLeft(PUZZLE_TIME_LIMIT_SEC);
    setErrors(0);
    setDangerPulse(0);
    setDangerText(dangerLabelByPuzzle());
    onError(null);
  };

  return (
    <li className={`${styles.taskItem} ${styles.mine} ${styles[`s_${task.status}` as keyof typeof styles] ?? ""}`}>
      <div className={styles.taskHeader}>
        <span className={styles.taskTitle}>{task.title}</span>
        <div className={styles.badges}>
          <span className={`${styles.statusBadge} ${styles[`s_${task.status}` as keyof typeof styles] ?? ""}`}>
            {STATUS_LABEL[task.status]}
          </span>
          {task.critical ? <span className={styles.criticalBadge}>Crítica</span> : null}
        </div>
      </div>
      {task.description ? <p className={styles.taskDesc}>{task.description}</p> : null}
      {canPlayPuzzle && task.status !== "DONE" ? (
        <p className={styles.puzzleTypeHint}>Minigame: {PUZZLE_LABELS[puzzleType]}</p>
      ) : null}
      {task.critical && !puzzleType && task.status !== "DONE" ? (
        <p className={styles.puzzleTypeHint}>Minigame não configurado — recrie a tarefa na sala de comando.</p>
      ) : null}

      {task.status === "PENDING" ? (
        <button type="button" className={styles.btnAction} disabled={busy} onClick={() => void run("START")}>
          Iniciar tarefa
        </button>
      ) : null}

      {task.status === "IN_PROGRESS" ? (
        <button type="button" className={styles.btnAction} disabled={busy} onClick={() => void run("COMPLETE")}>
          {task.critical ? "Ir para o minigame" : "Concluir tarefa"}
        </button>
      ) : null}

      {task.status === "AWAITING_PUZZLE" && canPlayPuzzle ? (
        <>
          <button type="button" className={styles.btnAction} disabled={busy} onClick={openPuzzle}>
            {puzzleOpen ? "Continuar minigame" : "Iniciar puzzle"}
          </button>
          {puzzleOpen ? (
            <PuzzleModal
              title={PUZZLE_LABELS[puzzleType]}
              subtitle="Resolva dentro do tempo limite. Você pode sair a qualquer momento."
              timeLeftSec={timeLeft}
              timeLimitSec={PUZZLE_TIME_LIMIT_SEC}
              errors={errors}
              maxErrors={PUZZLE_MAX_ERRORS}
              dangerPulse={dangerPulse}
              dangerText={dangerText}
              failureTier={failureTier}
              gameOverMessage={gameOverMessage}
              size={puzzleType === "CARD_MATCH" ? "cards" : "default"}
              onClose={() => { if (!puzzleLocked) setPuzzleOpen(false); }}
            >
              <MissionTaskPuzzle
                puzzleType={puzzleType}
                seed={task.puzzleSeed!}
                busy={busy || puzzleLocked}
                onMistake={handlePuzzleMistakeWithMessage}
                onSubmit={submitPuzzle}
              />
            </PuzzleModal>
          ) : null}
        </>
      ) : null}

      {task.status === "DONE" ? <p className={styles.doneMsg}>Tarefa concluída.</p> : null}
    </li>
  );
}
