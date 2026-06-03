import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { api } from "../../services/api";
import type { Mission, MissionCombatState } from "../../types/pvp";
import styles from "./MissionsListPage.module.css";

type FilterMode = "ALL" | MissionCombatState;

const STATE_LABEL: Record<MissionCombatState, string> = {
  LOBBY: "Lobby",
  ACTIVE: "Ativa",
  NORMAL: "Normal",
  ALERTA_INFILTRACAO: "Alerta",
  EM_DUELO: "Em duelo",
  SABOTADA: "Sabotada",
  DERROTADA: "Derrotada",
  DEFENDIDA: "Defendida",
  SEM_CHEFE: "Sem chefe",
  EM_CRISE: "Em crise",
  COMPROMETIDA: "Comprometida"
};

const STATE_FILTERS: { id: FilterMode; label: string }[] = [
  { id: "ALL", label: "Todas" },
  { id: "LOBBY", label: "Lobby" },
  { id: "ACTIVE", label: "Ativa" },
  { id: "NORMAL", label: "Normal" },
  { id: "ALERTA_INFILTRACAO", label: "Alerta" },
  { id: "EM_DUELO", label: "Em duelo" },
  { id: "SABOTADA", label: "Sabotada" },
  { id: "DERROTADA", label: "Derrotada" },
  { id: "COMPROMETIDA", label: "Comprometida" }
];

export function MissionsListPage() {
  const { user } = useAuth();
  const isHero = user?.role === "HERO";
  const [rows, setRows] = useState<Mission[]>([]);
  const [filter, setFilter] = useState<FilterMode>("ALL");
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.listMissions();
      setRows(data);
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar.");
    } finally {
      setLoading(false);
    }
  }, [isHero]);

  useEffect(() => {
    void load();
  }, [load]);

  const filtered =
    filter === "ALL" ? rows : rows.filter((m) => m.combatState === filter);

  if (loading) return <LoadingState message="Carregando missões…" />;

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Operações"
        title="Missões"
        description={
          isHero
            ? "Missões em que você participa — como criador ou membro da equipe."
            : "Visão geral de todas as missões ativas no sistema."
        }
        actions={
          isHero ? (
            <Link className={styles.btnNew} to="/missoes/nova">
              + Nova missão
            </Link>
          ) : null
        }
      />

      {err ? <p className={styles.error}>{err}</p> : null}

      <SectionCard title="Lista de missões">
        <div className={styles.filters}>
          {STATE_FILTERS.map((f) => (
            <button
              key={f.id}
              type="button"
              className={`${styles.filterChip} ${filter === f.id ? styles.filterActive : ""}`}
              onClick={() => setFilter(f.id)}
            >
              {f.label}
            </button>
          ))}
        </div>

        {filtered.length === 0 ? (
          <EmptyState
            title="Nenhuma missão"
            hint={isHero ? "Crie uma nova missão para começar." : "Nenhuma missão neste filtro."}
          />
        ) : (
          <ul className={styles.list}>
            {filtered.map((m) => (
              <li key={m.id}>
                <Link to={`/missoes/${m.id}`} className={styles.row}>
                  <span className={styles.title}>{m.titulo}</span>
                  <span
                    className={`${styles.stateBadge} ${(styles as Record<string, string>)[`state_${m.combatState}`] ?? ""}`}
                  >
                    {STATE_LABEL[m.combatState]}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </SectionCard>
    </div>
  );
}
