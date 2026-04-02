import type { ReactNode } from "react";
import styles from "./EmptyState.module.css";

type Props = {
  title: string;
  hint?: ReactNode;
  action?: ReactNode;
};

export function EmptyState({ title, hint, action }: Props) {
  return (
    <div className={styles.root}>
      <div className={styles.icon} aria-hidden>
        ◈
      </div>
      <p className={styles.title}>{title}</p>
      {hint ? <p className={styles.hint}>{hint}</p> : null}
      {action ? <div className={styles.action}>{action}</div> : null}
    </div>
  );
}
