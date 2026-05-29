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
          INFILTRAÇÃO DETECTADA
        </h2>
        <p className={styles.lead}>
          Um vilão rompeu as barreiras da missão. A equipe precisa responder na arena de duelo
          antes que a sabotagem se espalhe.
        </p>
        <div className={styles.actions}>
          {duelId ? (
            <Link className={styles.btnPrimary} to={`/duelo/${duelId}`} onClick={onDismiss}>
              Defender na arena
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
