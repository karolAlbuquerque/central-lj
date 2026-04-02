import type { MissionHistoryEntry } from "../../types/mission";
import styles from "./Timeline.module.css";

function originClass(o: string): string {
  if (o === "API_REGISTRO") return styles.itemOrigemApi;
  if (o === "API_ATRIBUICAO") return styles.itemOrigemAtrib;
  if (o === "KAFKA_WORKFLOW") return styles.itemOrigemKafka;
  if (o === "KAFKA_WORKFLOW_ERRO") return styles.itemOrigemErro;
  return "";
}

type Props = {
  entries: MissionHistoryEntry[];
  emptyHint?: string;
};

export function Timeline({ entries, emptyHint = "Sem eventos registrados." }: Props) {
  if (entries.length === 0) {
    return <p className={styles.empty}>{emptyHint}</p>;
  }
  return (
    <div className={styles.timeline}>
      {entries.map((h) => (
        <article key={h.id} className={`${styles.item} ${originClass(h.origem)}`}>
          <div className={styles.time}>{new Date(h.ocorridoEm).toLocaleString()}</div>
          {h.mensagem ? <p className={styles.msg}>{h.mensagem}</p> : null}
          <div className={styles.transition}>
            {h.statusAnterior
              ? `${h.statusAnterior.replaceAll("_", " ")} → ${h.statusNovo.replaceAll("_", " ")}`
              : `→ ${h.statusNovo.replaceAll("_", " ")}`}{" "}
            <span className={styles.tag}>({h.origem})</span>
          </div>
        </article>
      ))}
    </div>
  );
}
