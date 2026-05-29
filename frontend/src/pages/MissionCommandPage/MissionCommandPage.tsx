import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatCard } from "../../components/StatCard/StatCard";
import { api } from "../../services/api";
import type { DuelSession, MissionDetail, MissionTask } from "../../types/pvp";
import { MissionInfiltrationAlert } from "../../components/MissionInfiltrationAlert/MissionInfiltrationAlert";
import { useMissionInfiltrationAlert } from "../../hooks/useMissionInfiltrationAlert";
import { PUZZLE_LABELS } from "../../utils/puzzle";
import styles from "./MissionCommandPage.module.css";

const STATUS_LABEL: Record<MissionTask["status"], string> = {
  PENDING: "Pendente",
  IN_PROGRESS: "Em andamento",
  AWAITING_PUZZLE: "Aguardando puzzle",
  DONE: "Concluída",
  BLOCKED: "Bloqueada"
};

const COMBAT_STATE_LABEL: Record<string, string> = {
  LOBBY: "Lobby",
  ACTIVE: "Ativa",
  NORMAL: "Normal",
  ALERTA_INFILTRACAO: "Alerta — Infiltração",
  EM_DUELO: "Em duelo",
  SABOTADA: "Sabotada",
  DEFENDIDA: "Defendida",
  SEM_CHEFE: "Sem líder",
  EM_CRISE: "Em crise",
  COMPROMETIDA: "Comprometida"
};

const COMBAT_STATE_CLASS: Record<string, string> = {
  SABOTADA: "metaValueDanger",
  EM_DUELO: "metaValueDanger",
  EM_CRISE: "metaValueDanger",
  COMPROMETIDA: "metaValueDanger",
  ALERTA_INFILTRACAO: "metaValueWarn",
  SEM_CHEFE: "metaValueWarn"
};

const ROLE_LABEL: Record<string, string> = {
  CHEFE: "Chefe",
  HERO_MEMBER: "Membro",
  VILLAIN_INTRUDER: "Intruso"
};

const ROLE_CLASS: Record<string, string> = {
  CHEFE: "memberChefe",
  VILLAIN_INTRUDER: "memberVillain"
};

const TASK_GROUP_ORDER: Array<MissionTask["status"]> = [
  "IN_PROGRESS", "AWAITING_PUZZLE", "PENDING", "BLOCKED", "DONE"
];

export function MissionCommandPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [detail, setDetail] = useState<MissionDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [inviteUserId, setInviteUserId] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [activeDuel, setActiveDuel] = useState<DuelSession | null>(null);
  const { showAlert, duelId: alertDuelId, dismiss: dismissAlert } = useMissionInfiltrationAlert(id);

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setErr(null);
    try {
      const [missionDetail, duel] = await Promise.all([
        api.getMissionDetail(id),
        api.getActiveDuelForMission(id).catch(() => null)
      ]);
      setDetail(missionDetail);
      setActiveDuel(duel);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar missão.");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { void load(); }, [load]);

  const handleInvite = async () => {
    if (!id || !inviteUserId.trim()) return;
    setSubmitting(true);
    try {
      await api.inviteMissionMember(id, inviteUserId.trim());
      setInviteUserId("");
      await load();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao convidar.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleStartMission = async (forceStart: boolean) => {
    if (!id) return;
    setSubmitting(true);
    try {
      await api.startMission(id, forceStart);
      await load();
      navigate(`/missoes/${id}/tarefas`, { state: { playIntro: true } });
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao iniciar missão.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleAcceptInvite = async () => {
    if (!id) return;
    setSubmitting(true);
    try {
      await api.acceptMissionInvite(id);
      await load();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao aceitar convite.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeclineInvite = async () => {
    if (!id) return;
    setSubmitting(true);
    try {
      await api.declineMissionInvite(id);
      await load();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao recusar convite.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingState message="Carregando sala de comando…" />;
  if (!detail) return <EmptyState title="Missão não encontrada" />;

  const { mission, members, tasks } = detail;
  const isChefe = members.some(m => m.userId === user?.id && m.role === "CHEFE");
  const myMembership = members.find(m => m.userId === user?.id);
  const acceptedCount = members.filter(m => m.inviteStatus === "ACCEPTED").length;
  const doneTasks = tasks.filter(t => t.status === "DONE").length;
  const activeTasks = tasks.filter(t => t.status === "IN_PROGRESS" || t.status === "AWAITING_PUZZLE").length;

  const ownerName =
    members.find(m => m.userId === mission.ownerUserId)?.userName ??
    (user?.id === mission.ownerUserId ? user.nome : `${mission.ownerUserId.slice(0, 8)}…`);

  const createdDate = new Date(mission.createdAt).toLocaleDateString("pt-BR", {
    day: "2-digit", month: "short", year: "numeric"
  });

  const combatLabel = COMBAT_STATE_LABEL[mission.combatState] ?? mission.combatState.replace(/_/g, " ");
  const combatClass = COMBAT_STATE_CLASS[mission.combatState];

  return (
    <div className={styles.page}>
      {showAlert && id ? (
        <MissionInfiltrationAlert missionId={id} duelId={alertDuelId ?? activeDuel?.id ?? null} onDismiss={dismissAlert} />
      ) : null}
      <PageHeader
        kicker="Sala de comando"
        title={mission.titulo}
        description={mission.descricao ?? undefined}
        actions={
          <Link className={styles.btnSecondary} to={`/missoes/${id}/tarefas`}>
            Executar tarefas
          </Link>
        }
      />

      {/* Mission identity strip */}
      <div className={styles.missionMeta}>
        <div className={styles.metaGroup}>
          <span className={styles.metaLabel}>Criado por</span>
          <span className={styles.metaValue}>{ownerName}</span>
        </div>
        <span className={styles.metaDot} aria-hidden>·</span>
        <div className={styles.metaGroup}>
          <span className={styles.metaLabel}>Estado</span>
          <span className={`${styles.metaValue} ${combatClass ? styles[combatClass as keyof typeof styles] : ""}`}>
            {combatLabel}
          </span>
        </div>
        <span className={styles.metaDot} aria-hidden>·</span>
        <div className={styles.metaGroup}>
          <span className={styles.metaLabel}>Criada em</span>
          <span className={styles.metaValue}>{createdDate}</span>
        </div>
      </div>

      <div className={styles.metrics}>
        <StatCard label="Membros" value={members.length} variant="info" />
        <StatCard label="Tarefas" value={tasks.length} variant="info" />
        <StatCard label="Concluídas" value={doneTasks} variant="success" />
        <StatCard label="Em andamento" value={activeTasks} variant={activeTasks > 0 ? "info" : "default"} />
      </div>

      {err ? <p className={styles.error}>{err}</p> : null}

      {activeDuel &&
      (mission.combatState === "ALERTA_INFILTRACAO" || mission.combatState === "EM_DUELO") ? (
        <SectionCard
          title="Duelo em andamento"
          hint={
            activeDuel.status === "PENDING"
              ? "Brecha concluída — o chefe deve aceitar o duelo na arena."
              : "Combate de puzzles em andamento na arena."
          }
        >
          <Link className={styles.btnPrimary} to={`/duelo/${activeDuel.id}`}>
            {user?.id === activeDuel.defenderUserId && activeDuel.status === "PENDING"
              ? "Entrar na arena de duelo"
              : "Abrir arena de duelo"}
          </Link>
        </SectionCard>
      ) : null}

      <div className={styles.contentGrid}>
        {/* ── Left: team ── */}
        <div>
          <SectionCard
            title="Equipe"
            hint={`${acceptedCount} aceito(s) · mínimo ${mission.minPlayers}`}
          >
            {members.length === 0 ? (
              <EmptyState title="Nenhum membro" hint="Convide heróis para a missão." />
            ) : (
              <ul className={styles.memberList}>
                {members.map(m => (
                  <li
                    key={m.id}
                    className={[
                      styles.memberItem,
                      ROLE_CLASS[m.role] ? styles[ROLE_CLASS[m.role] as keyof typeof styles] : ""
                    ].filter(Boolean).join(" ")}
                  >
                    <span className={styles.memberName}>{m.userName}</span>
                    <span className={styles.memberRole}>
                      {ROLE_LABEL[m.role] ?? m.role} · {m.inviteStatus}
                    </span>
                  </li>
                ))}
              </ul>
            )}
            {mission.combatState === "LOBBY" && myMembership?.inviteStatus === "PENDING" ? (
              <div className={styles.actionsRow}>
                <button
                  type="button"
                  className={styles.btnPrimary}
                  disabled={submitting}
                  onClick={() => { void handleAcceptInvite(); }}
                >
                  Aceitar convite
                </button>
                <button
                  type="button"
                  className={styles.btnSecondary}
                  disabled={submitting}
                  onClick={() => { void handleDeclineInvite(); }}
                >
                  Recusar
                </button>
              </div>
            ) : null}
            {isChefe ? (
              <div className={styles.inviteRow}>
                <input
                  className={styles.input}
                  placeholder="UUID do herói"
                  value={inviteUserId}
                  onChange={e => setInviteUserId(e.target.value)}
                />
                <button
                  type="button"
                  className={styles.btnPrimary}
                  disabled={submitting || !inviteUserId.trim()}
                  onClick={() => { void handleInvite(); }}
                >
                  Convidar
                </button>
              </div>
            ) : null}
            {isChefe && mission.combatState === "LOBBY" ? (
              <div className={styles.actionsRow}>
                <button
                  type="button"
                  className={styles.btnPrimary}
                  disabled={submitting || acceptedCount < mission.minPlayers}
                  onClick={() => { void handleStartMission(false); }}
                >
                  Iniciar missão ({acceptedCount}/{mission.minPlayers})
                </button>
                <button
                  type="button"
                  className={styles.btnSecondary}
                  disabled={submitting}
                  onClick={() => { void handleStartMission(true); }}
                >
                  Forçar início
                </button>
              </div>
            ) : null}
          </SectionCard>
        </div>

        {/* ── Right: tasks ── */}
        <div>
          <SectionCard
            title="Tarefas"
            hint={tasks.length > 0
              ? `${doneTasks} de ${tasks.length} concluída${doneTasks !== 1 ? "s" : ""}`
              : undefined}
          >
            <p className={styles.taskCreateLabel}>
              Puzzles são gerados automaticamente no início da missão (3 por membro aceito).
            </p>

            {tasks.length === 0 ? (
              <EmptyState title="Nenhuma tarefa" hint="O chefe ainda não criou tarefas." />
            ) : (
              <div className={styles.taskGroups}>
                {TASK_GROUP_ORDER.map(status => {
                  const group = tasks.filter(t => t.status === status);
                  if (group.length === 0) return null;
                  return (
                    <div key={status} className={`${styles.taskGroup} ${styles[`group_${status}` as keyof typeof styles] ?? ""}`}>
                      <div className={styles.taskGroupHeader}>
                        <span className={styles.taskGroupLabel}>{STATUS_LABEL[status]}</span>
                        <span className={styles.taskGroupCount}>{group.length}</span>
                        <span className={styles.taskGroupLine} />
                      </div>
                      <div className={styles.taskGrid}>
                        {group.map(t => (
                          <div
                            key={t.id}
                            className={`${styles.taskCard} ${styles[`task_${t.status}` as keyof typeof styles] ?? ""}`}
                          >
                            <span className={styles.taskTitle}>{t.title}</span>
                            <div className={styles.taskBadges}>
                              {t.critical ? <span className={styles.criticalBadge}>Crítica</span> : null}
                              {t.critical && t.puzzleType ? (
                                <span className={styles.puzzleTypeTag}>{PUZZLE_LABELS[t.puzzleType]}</span>
                              ) : null}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </SectionCard>
        </div>
      </div>
    </div>
  );
}
