import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { PriorityBadge } from "../../components/PriorityBadge/PriorityBadge";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { api } from "../../services/api";
import type { Mission } from "../../types/mission";
import styles from "./HeroMissionsPage.module.css";

function rowClass(m: Mission): string {
  if (m.prioridade === "CRITICA") return styles.rowCrit;
  if (m.prioridade === "ALTA") return styles.rowAlta;
  return "";
}

export function HeroMissionsPage() {
  const [rows, setRows] = useState<Mission[]>([]);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setRows(await api.listMyMissions());
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar.");
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Operação"
        title="Minhas missões"
        description="Missões em que você está designado como responsável. Prioridades críticas e altas ganham destaque visual."
      />
      {err ? <p className={styles.error}>{err}</p> : null}

      {rows.length === 0 && !err ? (
        <EmptyState
          title="Nenhuma missão atribuída"
          hint="Quando a coordenação designar você como responsável, as operações aparecerão nesta lista."
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
                <th>Atualização</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((m) => (
                <tr key={m.id} className={rowClass(m)}>
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
                    {new Date(m.ultimaAtualizacao).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </div>
  );
}
