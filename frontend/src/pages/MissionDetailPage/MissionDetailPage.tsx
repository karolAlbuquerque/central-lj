import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { useMissionUpdates } from "../../hooks/useMissionUpdates";
import { api } from "../../services/api";
import type { MissionDetail } from "../../types/mission";
import styles from "./MissionDetailPage.module.css";

function originClass(o: string): string {
  if (o === "API_REGISTRO") return styles.itemOrigemApi;
  if (o === "KAFKA_WORKFLOW_ERRO") return styles.itemOrigemErro;
  return "";
}

export function MissionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<MissionDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!id) return;
    try {
      const d = await api.getMissionDetail(id);
      setDetail(d);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao carregar.");
    }
  }, [id]);

  useMissionUpdates(load, 12000);

  if (!id) {
    return <p className={styles.error}>ID inválido.</p>;
  }

  if (error) {
    return (
      <div className={styles.page}>
        <Link className={styles.back} to="/">
          ← Painel
        </Link>
        <p className={styles.error}>{error}</p>
      </div>
    );
  }

  if (!detail) {
    return (
      <div className={styles.page}>
        <Link className={styles.back} to="/">
          ← Painel
        </Link>
        <p className={styles.sectionHint}>Carregando…</p>
      </div>
    );
  }

  const { missao: m, historico } = detail;

  return (
    <div className={styles.page}>
      <Link className={styles.back} to="/">
        ← Voltar ao painel
      </Link>

      <div className={styles.grid}>
        <section className={styles.panel}>
          <div style={{ display: "flex", alignItems: "flex-start", gap: "12px", flexWrap: "wrap" }}>
            <h1 className={styles.title}>{m.titulo}</h1>
            <StatusBadge status={m.status} />
          </div>
          <div className={styles.meta}>
            <div>
              <strong>Prioridade</strong> {m.prioridade}
            </div>
            <div>
              <strong>Ameaça</strong> {m.tipoAmeaca.replaceAll("_", " ")}
            </div>
            <div>
              <strong>Criada</strong> {new Date(m.dataCriacao).toLocaleString()}
            </div>
            <div>
              <strong>Última atualização</strong> {new Date(m.ultimaAtualizacao).toLocaleString()}
            </div>
            {m.localizacao &&
              (m.localizacao.cidade || m.localizacao.bairro || m.localizacao.referencia) && (
                <div>
                  <strong>Local</strong>{" "}
                  {[m.localizacao.cidade, m.localizacao.bairro, m.localizacao.referencia]
                    .filter(Boolean)
                    .join(" · ")}
                </div>
              )}
          </div>
          {m.descricao && <p className={styles.desc}>{m.descricao}</p>}
          <p className={styles.mono}>id: {m.id}</p>
        </section>

        <section className={styles.panel}>
          <h2 className={styles.sectionTitle}>Linha do tempo</h2>
          <p className={styles.sectionHint}>
            Histórico persistido a cada transição (API e pipeline Kafka). Atualização em tempo quase real
            via SSE + polling de backup.
          </p>
          {historico.length === 0 ? (
            <p className={styles.sectionHint}>Sem eventos registrados.</p>
          ) : (
            <div className={styles.timeline}>
              {historico.map((h) => (
                <article key={h.id} className={`${styles.item} ${originClass(h.origem)}`}>
                  <div className={styles.time}>{new Date(h.ocorridoEm).toLocaleString()}</div>
                  {h.mensagem && <p className={styles.msg}>{h.mensagem}</p>}
                  <div className={styles.transition}>
                    {h.statusAnterior
                      ? `${h.statusAnterior.replaceAll("_", " ")} → ${h.statusNovo.replaceAll("_", " ")}`
                      : `→ ${h.statusNovo.replaceAll("_", " ")}`}{" "}
                    <span className={styles.sectionHint}>({h.origem})</span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
