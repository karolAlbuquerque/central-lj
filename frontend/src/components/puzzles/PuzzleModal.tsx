import { useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";
import { createPortal } from "react-dom";
import styles from "./puzzles.module.css";

type FailureTier = "none" | "hint" | "fatal";

type Props = {
  title: string;
  subtitle?: string;
  timeLeftSec: number;
  timeLimitSec?: number;
  errors: number;
  maxErrors: number;
  dangerPulse?: number;
  dangerText?: string;
  failureTier?: FailureTier;
  gameOverMessage?: string | null;
  size?: "default" | "cards";
  onClose: () => void;
  children: ReactNode;
};

export function PuzzleModal({
  title,
  subtitle,
  timeLeftSec,
  timeLimitSec = 75,
  errors,
  maxErrors,
  dangerPulse = 0,
  dangerText = "ERRO!",
  failureTier = "none",
  gameOverMessage = null,
  size = "default",
  onClose,
  children
}: Props) {
  const phaserHostRef = useRef<HTMLDivElement | null>(null);
  const [flashVisible, setFlashVisible] = useState(false);
  const flashTimerRef = useRef<number | null>(null);

  const isFatal = failureTier === "fatal";
  const timePct = Math.max(0, Math.min(100, (timeLeftSec / timeLimitSec) * 100));
  const errorPct = Math.max(0, Math.min(100, (errors / maxErrors) * 100));

  useEffect(() => {
    let game: { destroy: (removeCanvas: boolean) => void } | undefined;
    let cancelled = false;

    void (async () => {
      const Phaser = (await import("phaser")).default;
      if (cancelled || !phaserHostRef.current) return;

      class BgScene extends Phaser.Scene {
        create() {
          const width = this.scale.width;
          const height = this.scale.height;
          for (let i = 0; i < 32; i++) {
            const dot = this.add.circle(
              Phaser.Math.Between(0, width),
              Phaser.Math.Between(0, height),
              Phaser.Math.Between(2, 6),
              i % 3 === 0 ? 0x2ee59d : 0x00d1ff,
              Phaser.Math.FloatBetween(0.15, 0.55)
            );
            this.tweens.add({
              targets: dot,
              y: { from: dot.y, to: dot.y - Phaser.Math.Between(30, 100) },
              alpha: { from: dot.alpha, to: 0.04 },
              duration: Phaser.Math.Between(2000, 4000),
              yoyo: true,
              repeat: -1,
              ease: "Sine.easeInOut"
            });
          }
        }
      }

      game = new Phaser.Game({
        type: Phaser.AUTO,
        width: 900,
        height: 520,
        parent: phaserHostRef.current,
        transparent: true,
        scene: BgScene,
        fps: { target: 60, forceSetTimeOut: true }
      });
    })();

    return () => {
      cancelled = true;
      if (game) game.destroy(true);
    };
  }, []);

  useEffect(() => {
    if (dangerPulse <= 0) return undefined;

    if (flashTimerRef.current) {
      window.clearTimeout(flashTimerRef.current);
      flashTimerRef.current = null;
    }

    setFlashVisible(true);
    const holdMs = isFatal ? 3000 : 520;

    flashTimerRef.current = window.setTimeout(() => {
      if (!isFatal) setFlashVisible(false);
      flashTimerRef.current = null;
    }, holdMs);

    return () => {
      if (flashTimerRef.current) {
        window.clearTimeout(flashTimerRef.current);
        flashTimerRef.current = null;
      }
    };
  }, [dangerPulse, isFatal]);

  useEffect(() => {
    return () => {
      if (flashTimerRef.current) window.clearTimeout(flashTimerRef.current);
    };
  }, []);

  const content = (
    <div className={styles.modalBackdrop}>
      <div
        className={`${styles.modalCard} ${size === "cards" ? styles.modalCardCards : ""} ${
          isFatal ? styles.modalCardFatalBorder : ""
        }`}
      >
        <div className={`${styles.modalShakeLayer} ${isFatal ? styles.modalShakeLayerFatal : ""}`}>
          <div ref={phaserHostRef} className={styles.modalPhaserBg} />

          {flashVisible ? (
            <div
              key={`flash-${dangerPulse}`}
              className={`${styles.modalDangerFlash} ${isFatal ? styles.modalDangerFlashFatal : styles.modalDangerFlashHint}`}
              aria-hidden
            >
              <span className={styles.modalDangerFlashText}>{dangerText}</span>
            </div>
          ) : null}

          <button type="button" className={styles.modalClose} onClick={onClose} aria-label="Fechar puzzle">
            ×
          </button>

          <div className={styles.modalHeader}>
            <h3 className={styles.modalTitle}>{title}</h3>
            {subtitle ? <p className={styles.modalSubtitle}>{subtitle}</p> : null}
            <div className={styles.modalStats}>
              <div className={styles.statBlock}>
                <span className={styles.statLabel}>Tempo restante</span>
                <div className={styles.statBar}>
                  <div
                    className={`${styles.statBarFill} ${styles.time}`}
                    style={{ width: `${timePct}%` }}
                  />
                </div>
                <span className={styles.statValue}>{timeLeftSec}s</span>
              </div>
              <div className={styles.statBlock}>
                <span className={styles.statLabel}>Erros</span>
                <div className={styles.statBar}>
                  <div
                    className={`${styles.statBarFill} ${styles.errors}`}
                    style={{ width: `${errorPct}%` }}
                  />
                </div>
                <span className={styles.statValue}>
                  {errors} / {maxErrors}
                </span>
              </div>
            </div>
          </div>

          {gameOverMessage ? (
            <div className={styles.modalGameOver} role="alert">
              <p className={styles.modalGameOverTitle}>Minigame encerrado</p>
              <p className={styles.modalGameOverText}>{gameOverMessage}</p>
            </div>
          ) : null}

          <div className={styles.modalBody}>{children}</div>
        </div>
      </div>
    </div>
  );

  if (typeof document === "undefined") {
    return content;
  }
  return createPortal(content, document.body);
}
