import styles from "./LoadingState.module.css";

type Props = {
  message?: string;
};

export function LoadingState({ message = "Carregando dados operacionais…" }: Props) {
  return (
    <div className={styles.root} role="status" aria-live="polite">
      <div className={styles.pulse} aria-hidden />
      <p className={styles.msg}>{message}</p>
    </div>
  );
}
