import { lazy, Suspense, useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { N1DevTools } from "../../components/N1DevTools/N1DevTools";

const BatmanViewer = lazy(() =>
  import("../../components/BatmanViewer/BatmanViewer").then((m) => ({ default: m.BatmanViewer }))
);
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatCard } from "../../components/StatCard/StatCard";
import { api } from "../../services/api";
import type { Equipe, Hero } from "../../types/mission";
import type { Mission, MissionCombatState } from "../../types/pvp";
import styles from "./DashboardPage.module.css";

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

export function DashboardPage() {
  const [missions, setMissions] = useState<Mission[]>([]);
  const [heroes, setHeroes] = useState<Hero[]>([]);
  const [teams, setTeams] = useState<Equipe[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const [missionRows, heroesRows, teamsRows] = await Promise.all([
        api.listMissions(),
        api.listHeroes(),
        api.listTeams()
      ]);
      setMissions(missionRows);
      setHeroes(heroesRows);
      setTeams(teamsRows);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao carregar painel.");
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const heroStats = useMemo(() => {
    const ativos = heroes.filter((h) => h.ativo);
    return {
      disponiveis: ativos.filter((h) => h.statusDisponibilidade === "DISPONIVEL").length,
      emMissao: ativos.filter((h) => h.statusDisponibilidade === "EM_MISSAO").length,
      totalAtivos: ativos.length
    };
  }, [heroes]);

  const teamsAtivas = useMemo(() => teams.filter((t) => t.ativa).length, [teams]);

  const emAlerta = useMemo(
    () =>
      missions.filter((m) =>
        ["ALERTA_INFILTRACAO", "EM_DUELO", "SABOTADA", "DERROTADA", "EM_CRISE"].includes(m.combatState)
      ).length,
    [missions]
  );

  const recentes = useMemo(
    () =>
      [...missions]
        .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
        .slice(0, 8),
    [missions]
  );

  const stateKind = (s: MissionCombatState): "danger" | "warn" | null => {
    if (["ALERTA_INFILTRACAO", "EM_DUELO", "EM_CRISE", "COMPROMETIDA"].includes(s)) return "danger";
    if (["SABOTADA", "DERROTADA", "SEM_CHEFE"].includes(s)) return "warn";
    return null;
  };

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Painel operacional"
        title="Situação da central"
        description="Visão consolidada de missões, elenco heroico e equipes."
      />

      {error ? <p className={styles.errorBanner}>{error}</p> : null}

      <div className={styles.metrics}>
        <StatCard label="Missões" value={missions.length} variant="info" />
        <StatCard label="Em alerta / duelo" value={emAlerta} variant="warn" />
        <StatCard label="Heróis ativos" value={heroStats.totalAtivos} variant="success" />
        <StatCard label="Equipes ativas" value={teamsAtivas} variant="info" />
      </div>

      <div className={styles.dashboardShell}>
        <div className={styles.mainColumn}>
          <SectionCard title="Missões recentes" hint="Últimas atualizações no sistema">
            {recentes.length === 0 ? (
              <p className={styles.muted}>Nenhuma missão registrada.</p>
            ) : (
              <ul className={styles.recentList}>
                {recentes.map((m) => {
                  const kind = stateKind(m.combatState);
                  return (
                    <li key={m.id}>
                      {kind ? <span className={styles.alertDot} data-kind={kind} aria-hidden /> : <span className={styles.alertDotNeutral} aria-hidden />}
                      <Link className={styles.missionLink} to={`/missoes/${m.id}`}>
                        {m.titulo}
                      </Link>
                      <span className={`${styles.missionMeta} ${kind === "danger" ? styles.metaDanger : kind === "warn" ? styles.metaWarn : ""}`}>
                        {STATE_LABEL[m.combatState]}
                      </span>
                    </li>
                  );
                })}
              </ul>
            )}
            <Link className={styles.btnGhost} to="/missoes">
              Ver todas as missões
            </Link>
          </SectionCard>

          <SectionCard title="Elenco" hint="Disponibilidade atual">
            <div className={styles.crewGrid}>
              <div className={styles.crewItem}>
                <span className={styles.crewValue}>{heroStats.disponiveis}</span>
                <span className={styles.crewLabel}>Disponíveis</span>
              </div>
              <div className={styles.crewItem}>
                <span className={`${styles.crewValue} ${styles.crewValueActive}`}>{heroStats.emMissao}</span>
                <span className={styles.crewLabel}>Em missão</span>
              </div>
              <div className={styles.crewItem}>
                <span className={styles.crewValue}>{heroStats.totalAtivos}</span>
                <span className={styles.crewLabel}>Total ativos</span>
              </div>
            </div>
          </SectionCard>

          <N1DevTools />
        </div>

        <aside className={styles.batmanColumn}>
          <Suspense fallback={null}>
            <BatmanViewer />
          </Suspense>
        </aside>
      </div>
    </div>
  );
}
