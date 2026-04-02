import type { ReactNode } from "react";
import styles from "./PageHeader.module.css";

type Props = {
  kicker?: string;
  title: string;
  description?: ReactNode;
  actions?: ReactNode;
};

export function PageHeader({ kicker, title, description, actions }: Props) {
  return (
    <header className={styles.root}>
      <div className={styles.text}>
        {kicker ? <p className={styles.kicker}>{kicker}</p> : null}
        <h1 className={styles.title}>{title}</h1>
        {description ? <div className={styles.desc}>{description}</div> : null}
      </div>
      {actions ? <div className={styles.actions}>{actions}</div> : null}
    </header>
  );
}
