import type { HeroiDisponibilidade } from "../../types/mission";
import styles from "./HeroAvailabilityBadge.module.css";

const CLS: Record<HeroiDisponibilidade, string> = {
  DISPONIVEL: styles.disp,
  EM_MISSAO: styles.missao,
  INATIVO: styles.inat
};

export function HeroAvailabilityBadge({ status }: { status: HeroiDisponibilidade }) {
  const label =
    status === "EM_MISSAO"
      ? "Em missão"
      : status === "DISPONIVEL"
        ? "Disponível"
        : "Inativo";
  return <span className={`${styles.badge} ${CLS[status]}`}>{label}</span>;
}
