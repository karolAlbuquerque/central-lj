import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { ModelViewer } from "../../components/ModelViewer/ModelViewer";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { PriorityBadge } from "../../components/PriorityBadge/PriorityBadge";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { api } from "../../services/api";
import type { Mission, MissionStatus } from "../../types/mission";
import styles from "./MissionsListPage.module.css";

type FilterMode = "ALL" | MissionStatus;

const STATUS_FILTERS: { id: FilterMode; label: string }[] = [
  { id: "ALL", label: "Todas" },
  { id: "RECEBIDA", label: "Recebidas" },
  { id: "EM_ANALISE", label: "Em análise" },
  { id: "PRIORIZADA", label: "Priorizadas" },
  { id: "EQUIPE_DESIGNADA", label: "Equipe designada" },
  { id: "EM_ANDAMENTO", label: "Em andamento" },
  { id: "CONCLUIDA", label: "Concluídas" },
  { id: "FALHA_PROCESSAMENTO", label: "Falhas" }
];

const BATARANG_MODEL = "/batman_batarang_-_ben_affleck.glb";

export function MissionsListPage() {
  const [rows, setRows] = useState<Mission[]>([]);
  const [filter, setFilter] = useState<FilterMode>("ALL");
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data =
        filter === "ALL" ? await api.listMissions() : await api.listMissionsByStatus(filter);
      setRows(data);
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar.");
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Registro operacional"
        title="Missões"
        description="Todas as ocorrências na central, com filtro por status e leitura clara de prioridade e designação."
        actions={
          <Link className={styles.btnPrimary} to="/operacoes/nova">
            + Nova missão
          </Link>
        }
      />

      <div className={styles.filterBar}>
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.id}
            type="button"
            className={`${styles.chip} ${filter === f.id ? styles.chipActive : ""}`}
            onClick={() => setFilter(f.id)}
          >
            {f.label}
          </button>
        ))}
      </div>

      {err ? <p className={styles.error}>{err}</p> : null}
      {loading ? <p className={styles.muted}>Carregando…</p> : null}

      {!loading && rows.length === 0 && !err ? (
        <EmptyState
          title="Nenhuma missão neste filtro"
          hint="Crie uma nova ocorrência ou ajuste o filtro de status."
          action={
            <Link className={styles.btnGhost} to="/operacoes/nova">
              Registrar missão
            </Link>
          }
        />
      ) : null}

      {rows.length > 0 ? (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Título</th>
                <th>Status</th>
                <th>Prioridade</th>
                <th>Ameaça</th>
                <th>Designação</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((m) => (
                <tr key={m.id} className={m.prioridade === "CRITICA" ? styles.rowCrit : ""}>
                  <td>
                    <Link className={styles.link} to={`/missoes/${m.id}`}>
                      {m.titulo}
                    </Link>
                  </td>
                  <td>
                    <StatusBadge status={m.status} />
                  </td>
                  <td>
                    <PriorityBadge prioridade={m.prioridade} />
                  </td>
                  <td className={styles.muted}>{m.tipoAmeaca.replaceAll("_", " ")}</td>
                  <td className={styles.muted}>
                    {m.atribuicao?.nomeHeroico ?? m.atribuicao?.nomeEquipe ?? "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      <div className={styles.batarangZone} aria-label="Batarang tática">
        <ModelViewer src={BATARANG_MODEL} alt="Batarang do Batman" />
      </div>
    </div>
  );
}
