import styles from "./MetricCard.module.css";

type Props = {
  label: string;
  value: number | string;
  hint?: string;
  variant?: "default" | "success" | "warn" | "danger";
};

export function MetricCard({ label, value, hint, variant = "default" }: Props) {
  return (
    <div className={`${styles.card} ${styles[variant]}`}>
      <div className={styles.label}>{label}</div>
      <div className={styles.value}>{value}</div>
      {hint ? <div className={styles.hint}>{hint}</div> : null}
    </div>
  );
}
