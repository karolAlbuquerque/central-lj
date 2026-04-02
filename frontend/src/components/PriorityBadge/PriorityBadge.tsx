import type { PrioridadeMissao } from "../../types/mission";
import styles from "./PriorityBadge.module.css";

const CLS: Record<PrioridadeMissao, string> = {
  BAIXA: styles.baixa,
  MEDIA: styles.media,
  ALTA: styles.alta,
  CRITICA: styles.critica
};

export function PriorityBadge({ prioridade }: { prioridade: PrioridadeMissao }) {
  return <span className={`${styles.badge} ${CLS[prioridade]}`}>{prioridade}</span>;
}
