import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import type { DuelOutcomeKind } from "../../utils/duelOutcome";
import styles from "./DuelOutcomeFlash.module.css";

type Props = {
  kind: DuelOutcomeKind;
  headline: string;
  subline?: string;
  pulse: number;
};

export function DuelOutcomeFlash({ kind, headline, subline, pulse }: Props) {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    setVisible(true);
    const t = window.setTimeout(() => setVisible(false), 3200);
    return () => window.clearTimeout(t);
  }, [pulse]);

  if (!visible) return null;

  const content = (
    <div
      className={`${styles.backdrop} ${kind === "win" ? styles.backdropWin : kind === "lose" ? styles.backdropLose : styles.backdropCancel}`}
      role="alertdialog"
      aria-live="assertive"
      aria-label={headline}
    >
      <div
        key={pulse}
        className={`${styles.flash} ${kind === "win" ? styles.flashWin : kind === "lose" ? styles.flashLose : styles.flashCancel}`}
      >
        <span className={styles.headline}>{headline}</span>
        {subline ? <span className={styles.subline}>{subline}</span> : null}
      </div>
    </div>
  );

  if (typeof document === "undefined") return content;
  return createPortal(content, document.body);
}
