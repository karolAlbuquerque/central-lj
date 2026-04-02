import type { ReactNode } from "react";
import styles from "./SectionCard.module.css";

type Props = {
  title: string;
  hint?: ReactNode;
  children: ReactNode;
  variant?: "default" | "alert" | "gold";
};

export function SectionCard({ title, hint, children, variant = "default" }: Props) {
  return (
    <section className={`${styles.card} ${styles[variant]}`}>
      <div className={styles.head}>
        <h2 className={styles.title}>{title}</h2>
        {hint ? <div className={styles.hint}>{hint}</div> : null}
      </div>
      <div className={styles.body}>{children}</div>
    </section>
  );
}
