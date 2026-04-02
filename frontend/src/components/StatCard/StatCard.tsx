import styles from "./StatCard.module.css";

export type StatCardVariant = "default" | "success" | "warn" | "danger" | "info" | "gold";

type Props = {
  label: string;
  value: number | string;
  hint?: string;
  variant?: StatCardVariant;
};

export function StatCard({ label, value, hint, variant = "default" }: Props) {
  return (
    <div className={`${styles.card} ${styles[variant]}`}>
      <div className={styles.label}>{label}</div>
      <div className={styles.value}>{value}</div>
      {hint ? <div className={styles.hint}>{hint}</div> : null}
    </div>
  );
}
