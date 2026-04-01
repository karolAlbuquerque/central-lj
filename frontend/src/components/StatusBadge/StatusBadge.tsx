import type { MissionStatus } from "../../types/mission";
import styles from "./StatusBadge.module.css";

const STATUS_CLASS: Record<MissionStatus, string> = {
  RECEBIDA: styles.recebida,
  EM_ANALISE: styles.emAnalise,
  PRIORIZADA: styles.priorizada,
  EQUIPE_DESIGNADA: styles.equipe,
  EM_ANDAMENTO: styles.emAndamento,
  CONCLUIDA: styles.concluida,
  FALHA_PROCESSAMENTO: styles.falha
};

function formatLabel(status: MissionStatus): string {
  return status.replaceAll("_", " ");
}

export function StatusBadge({ status }: { status: MissionStatus }) {
  return (
    <span className={`${styles.badge} ${STATUS_CLASS[status]}`}>{formatLabel(status)}</span>
  );
}
