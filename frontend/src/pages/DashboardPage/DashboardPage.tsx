import { lazy, Suspense, useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { N1DevTools } from "../../components/N1DevTools/N1DevTools";

const BatmanViewer = lazy(() =>
  import("../../components/BatmanViewer/BatmanViewer").then((m) => ({ default: m.BatmanViewer }))
);
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { PriorityBadge } from "../../components/PriorityBadge/PriorityBadge";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatCard } from "../../components/StatCard/StatCard";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { useMissionUpdates } from "../../hooks/useMissionUpdates";
import { api } from "../../services/api";
import type { DashboardSummary, Equipe, Hero, Mission, MissionStatus } from "../../types/mission";
import styles from "./DashboardPage.module.css";

function rowClass(m: Mission): string {
  if (m.prioridade === "CRITICA") return styles.rowCritical;
  if (m.prioridade === "ALTA") return styles.rowAlta;
  return "";
}

type TableFilter = "RECENT" | MissionStatus;

const FILTER_CHIPS: { id: TableFilter; label: string }[] = [
  { id: "RECENT", label: "Recentes" },
  { id: "RECEBIDA", label: "Recebidas" },
  { id: "EM_ANALISE", label: "Em análise" },
  { id: "CONCLUIDA", label: "Concluídas" },
  { id: "FALHA_PROCESSAMENTO", label: "Falhas" }
];

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [emAnaliseCount, setEmAnaliseCount] = useState<number | null>(null);
  const [heroes, setHeroes] = useState<Hero[]>([]);
  const [teams, setTeams] = useState<Equipe[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [tableFilter, setTableFilter] = useState<TableFilter>("RECENT");
  const [tableRows, setTableRows] = useState<Mission[]>([]);
  const [tableLoading, setTableLoading] = useState(false);
  const [tableError, setTableError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const [data, emAnaliseRows, heroesRows, teamsRows] = await Promise.all([
        api.getDashboardSummary(),
        api.listMissionsByStatus("EM_ANALISE"),
        api.listHeroes(),
        api.listTeams()
      ]);
      setSummary(data);
      setEmAnaliseCount(emAnaliseRows.length);
      setHeroes(heroesRows);
      setTeams(teamsRows);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao carregar painel.");
    }
  }, []);

  useMissionUpdates(load, 12000);

  const heroStats = useMemo(() => {
    const ativos = heroes.filter((h) => h.ativo);
    return {
      disponiveis: ativos.filter((h) => h.statusDisponibilidade === "DISPONIVEL").length,
      emMissao: ativos.filter((h) => h.statusDisponibilidade === "EM_MISSAO").length,
      totalAtivos: ativos.length
    };
  }, [heroes]);

  const teamsAtivas = useMemo(() => teams.filter((t) => t.ativa).length, [teams]);

  const criticas = useMemo(() => {
    if (!summary) return [];
    return summary.recentes.filter((m) => m.prioridade === "CRITICA" || m.prioridade === "ALTA");
  }, [summary]);

  useEffect(() => {
    let cancelled = false;
    async function syncTable() {
      if (tableFilter === "RECENT") {
        if (summary) {
          setTableRows(summary.recentes);
        }
        setTableLoading(false);
        setTableError(null);
        return;
      }
      setTableLoading(true);
      setTableError(null);
      try {
        const rows = await api.listMissionsByStatus(tableFilter);
        if (!cancelled) {
          setTableRows(rows);
        }
      } catch (e) {
        if (!cancelled) {
          setTableError(e instanceof Error ? e.message : "Falha ao filtrar.");
        }
      } finally {
        if (!cancelled) {
          setTableLoading(false);
        }
      }
    }
    void syncTable();
    return () => {
      cancelled = true;
    };
  }, [tableFilter, summary]);

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Painel operacional"
        title="Situação da central"
        description={
          <>
            Visão agregada em tempo quase real: <strong>SSE</strong> quando o consumer Kafka altera status,
            com polling de backup a cada 12s. Use os filtros para isolar status na demonstração.
          </>
        }
        actions={
          <Link className={styles.btnPrimary} to="/operacoes/nova">
            + Nova missão
          </Link>
        }
      />

      {error && <p className={styles.errorBanner}>{error}</p>}

      <div className={styles.dashboardShell}>
        <aside className={styles.batmanColumn} aria-label="Visualização tática">
          <Suspense fallback={<div className={styles.batmanFallback}>Carregando visualização 3D…</div>}>
            <BatmanViewer />
          </Suspense>
        </aside>
        <div className={styles.mainColumn}>
      {summary && emAnaliseCount !== null ? (
        <>
          <div className={styles.metrics}>
            <StatCard label="Total de missões" value={summary.totalMissaoes} variant="info" />
            <StatCard
              label="Em análise"
              value={emAnaliseCount}
              hint="Aguardando triagem e priorização"
              variant="gold"
            />
            <StatCard
              label="Em andamento"
              value={summary.emAndamento}
              hint="Pipeline operacional ativo"
              variant="warn"
            />
            <StatCard label="Concluídas" value={summary.concluidas} variant="success" />
            <StatCard label="Falhas" value={summary.falhas} variant="danger" />
          </div>

          {(summary.falhas > 0 || criticas.length > 0) && (
            <SectionCard
              title="Alertas e prioridades"
              variant="alert"
              hint="Requer atenção imediata da coordenação."
            >
              <ul className={styles.alertList}>
                {summary.falhas > 0 ? (
                  <li>
                    <span className={styles.alertDot} data-kind="danger" />
                    <strong>{summary.falhas}</strong> missão(ões) com falha de processamento — investigar pipeline
                    e reprocessar se necessário.
                  </li>
                ) : null}
                {criticas.length > 0 ? (
                  <li>
                    <span className={styles.alertDot} data-kind="warn" />
                    <strong>{criticas.length}</strong> ocorrência(s) recente(s) com prioridade alta ou crítica na
                    janela exibida.
                  </li>
                ) : null}
              </ul>
            </SectionCard>
          )}

          <div className={styles.twoCol}>
            <SectionCard
              title="Missões críticas / prioritárias"
              variant="gold"
              hint="Recorte das missões recentes com prioridade ALTA ou CRÍTICA."
            >
              {criticas.length === 0 ? (
                <p className={styles.muted}>Nenhuma missão prioritária na lista recente.</p>
              ) : (
                <ul className={styles.criticalList}>
                  {criticas.map((m) => (
                    <li key={m.id}>
                      <Link className={styles.missionLink} to={`/missoes/${m.id}`}>
                        {m.titulo}
                      </Link>
                      <PriorityBadge prioridade={m.prioridade} />
                      <StatusBadge status={m.status} />
                    </li>
                  ))}
                </ul>
              )}
            </SectionCard>

            <SectionCard
              title="Recursos & unidades"
              hint="Resumo do elenco ativo e das equipes em operação."
            >
              <div className={styles.resourceGrid}>
                <div className={styles.resourceItem}>
                  <span className={styles.resourceLabel}>Heróis disponíveis</span>
                  <span className={styles.resourceValue}>{heroStats.disponiveis}</span>
                  <span className={styles.resourceMeta}>de {heroStats.totalAtivos} ativos</span>
                </div>
                <div className={styles.resourceItem}>
                  <span className={styles.resourceLabel}>Heróis em missão</span>
                  <span className={styles.resourceValue}>{heroStats.emMissao}</span>
                  <span className={styles.resourceMeta}>status EM_MISSAO</span>
                </div>
                <div className={styles.resourceItem}>
                  <span className={styles.resourceLabel}>Equipes ativas</span>
                  <span className={styles.resourceValue}>{teamsAtivas}</span>
                  <span className={styles.resourceMeta}>de {teams.length} cadastradas</span>
                </div>
              </div>
            </SectionCard>
          </div>

          <SectionCard
            title="Lista operacional"
            hint={
              <>
                Prioridade <strong>ALTA</strong> e <strong>CRÍTICA</strong> destacadas — clique no título para ver
                histórico e linha do tempo.
              </>
            }
          >
            <div className={styles.filterBar}>
              <span className={styles.filterLabel}>Filtro</span>
              {FILTER_CHIPS.map((c) => (
                <button
                  key={c.id}
                  type="button"
                  className={`${styles.chip} ${tableFilter === c.id ? styles.chipActive : ""}`}
                  onClick={() => setTableFilter(c.id)}
                >
                  {c.label}
                </button>
              ))}
            </div>
            {tableLoading && <p className={styles.tableLoading}>Carregando lista…</p>}
            {tableError && <p className={styles.errorInline}>{tableError}</p>}
            {!tableLoading && tableRows.length === 0 && !tableError ? (
              <p className={styles.muted}>Nenhuma missão neste filtro.</p>
            ) : null}
            {tableRows.length > 0 ? (
              <div className={styles.tableWrap}>
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th>Missão</th>
                      <th>Prioridade</th>
                      <th>Ameaça</th>
                      <th>Status</th>
                      <th>Atualizado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {tableRows.map((m) => (
                      <tr key={m.id} className={rowClass(m)}>
                        <td>
                          <Link className={styles.missionLink} to={`/missoes/${m.id}`}>
                            {m.titulo}
                          </Link>
                        </td>
                        <td>
                          <PriorityBadge prioridade={m.prioridade} />
                        </td>
                        <td className={styles.muted}>{m.tipoAmeaca.replaceAll("_", " ")}</td>
                        <td>
                          <StatusBadge status={m.status} />
                        </td>
                        <td className={styles.muted}>
                          {new Date(m.ultimaAtualizacao).toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : null}
          </SectionCard>
        </>
      ) : !error ? (
        <p className={styles.muted}>Carregando painel…</p>
      ) : null}
        </div>
      </div>

      <N1DevTools />
    </div>
  );
}
