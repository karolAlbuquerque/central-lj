import { Link } from "react-router-dom";
import styles from "./MissionInfiltrationAlert.module.css";

type Props = {
  missionId: string;
  duelId: string | null;
  onDismiss: () => void;
};

export function MissionInfiltrationAlert({ missionId, duelId, onDismiss }: Props) {
  return (
    <div className={styles.overlay} role="alertdialog" aria-labelledby="infiltration-alert-title">
      <div className={styles.backdrop} aria-hidden />
      <div className={styles.panel}>
        <div className={styles.streaks} aria-hidden>
          <span className={styles.streak} />
          <span className={styles.streak} />
          <span className={styles.streak} />
        </div>
        <p className={styles.kicker}>Alerta crítico</p>
        <h2 id="infiltration-alert-title" className={styles.title}>
          SUA MISSÃO FOI INVADIDA
        </h2>
        <p className={styles.lead}>
          Um vilão concluiu as brechas de infiltração. Aceite o duelo na arena e resolva 3 puzzles
          antes que ele termine os dele — se perder, a missão fica derrotada.
        </p>
        <div className={styles.actions}>
          {duelId ? (
            <Link className={styles.btnPrimary} to={`/duelo/${duelId}`} onClick={onDismiss}>
              Aceitar duelo na arena
            </Link>
          ) : (
            <Link className={styles.btnPrimary} to={`/missoes/${missionId}`} onClick={onDismiss}>
              Ir para a missão
            </Link>
          )}
          <button type="button" className={styles.btnGhost} onClick={onDismiss}>
            Continuar na sala
          </button>
        </div>
      </div>
    </div>
  );
}
