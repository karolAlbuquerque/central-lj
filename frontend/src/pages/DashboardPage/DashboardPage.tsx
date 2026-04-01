import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { MetricCard } from "../../components/MetricCard/MetricCard";
import { N1DevTools } from "../../components/N1DevTools/N1DevTools";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { useMissionUpdates } from "../../hooks/useMissionUpdates";
import { api } from "../../services/api";
import type { DashboardSummary, Mission } from "../../types/mission";
import styles from "./DashboardPage.module.css";

function rowClass(m: Mission): string {
  if (m.prioridade === "CRITICA") return styles.rowCritical;
  if (m.prioridade === "ALTA") return styles.rowAlta;
  return "";
}

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const data = await api.getDashboardSummary();
      setSummary(data);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao carregar painel.");
    }
  }, []);

  useMissionUpdates(load, 12000);

  return (
    <div className={styles.page}>
      <header className={styles.hero}>
        <p className={styles.kicker}>Painel operacional</p>
        <h1 className={styles.title}>Situação da central</h1>
        <p className={styles.lead}>
          Visão agregada das missões em tempo quase real. Atualizações via{" "}
          <strong>SSE</strong> quando o consumer Kafka altera status, com polling de backup a cada 12s.
        </p>
        <div className={styles.actionsTop}>
          <Link className={styles.btnPrimary} to="/operacoes/nova">
            + Nova missão
          </Link>
        </div>
      </header>

      {error && <p className={styles.error}>{error}</p>}

      {summary && (
        <>
          <section className={styles.metrics}>
            <MetricCard label="Total de missões" value={summary.totalMissaoes} />
            <MetricCard
              label="Em andamento"
              value={summary.emAndamento}
              hint="Análise, priorização, equipe ou execução"
              variant="warn"
            />
            <MetricCard label="Concluídas" value={summary.concluidas} variant="success" />
            <MetricCard label="Falhas" value={summary.falhas} variant="danger" />
          </section>

          <section>
            <h2 className={styles.sectionTitle}>Missões recentes</h2>
            <p className={styles.sectionHint}>
              Prioridade <strong>ALTA</strong> e <strong>CRÍTICA</strong> destacadas — clique no título para
              ver histórico e linha do tempo.
            </p>
            {summary.recentes.length === 0 ? (
              <p className={styles.muted}>Nenhuma missão registrada ainda.</p>
            ) : (
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
                    {summary.recentes.map((m) => (
                      <tr key={m.id} className={rowClass(m)}>
                        <td>
                          <Link className={styles.missionLink} to={`/missoes/${m.id}`}>
                            {m.titulo}
                          </Link>
                        </td>
                        <td>{m.prioridade}</td>
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
            )}
          </section>
        </>
      )}

      <N1DevTools />
    </div>
  );
}
