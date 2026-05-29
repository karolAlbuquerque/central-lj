import { useEffect, useMemo, useState } from "react";
import { SIMON_SIZE, generateSimonSequence, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

const COLORS = ["#ef4444", "#22c55e", "#3b82f6", "#eab308"];
const STEP_MS = 520;

export function ColorSimonPuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const target = useMemo(() => generateSimonSequence(seed), [seed]);
  const [input, setInput] = useState<number[]>([]);
  const [revealIdx, setRevealIdx] = useState<number>(-1);
  const [revealing, setRevealing] = useState(true);

  useEffect(() => {
    setInput([]);
    setRevealIdx(-1);
    setRevealing(true);
    let step = 0;
    const timer = window.setInterval(() => {
      if (step >= target.length) {
        window.clearInterval(timer);
        setRevealIdx(-1);
        setRevealing(false);
        return;
      }
      setRevealIdx(step);
      step += 1;
    }, STEP_MS);
    return () => window.clearInterval(timer);
  }, [target]);

  const onPress = (value: number) => {
    if (busy || revealing) return;
    const next = [...input, value];
    const idx = next.length - 1;
    if (target[idx] !== value) {
      onMistake?.("SEQUÊNCIA DE CORES INVÁLIDA!");
      setInput([]);
      return;
    }
    setInput(next);
  };

  const solved = input.length === SIMON_SIZE;

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Memorize a sequência de luzes e repita na mesma ordem.
      </p>
      <div className={styles.gamePanel}>
        <div className={styles.simonStatus}>
          {revealing ? "Observando sequência..." : `Acertos: ${input.length}/${SIMON_SIZE}`}
        </div>
        <div className={styles.simonGrid}>
          {COLORS.map((color, idx) => {
            const isLit = revealIdx >= 0 && target[revealIdx] === idx;
            return (
              <button
                key={idx}
                type="button"
                className={`${styles.simonPad} ${isLit ? styles.simonPadLit : ""}`}
                style={{ background: color }}
                disabled={busy || revealing}
                onClick={() => onPress(idx)}
              />
            );
          })}
        </div>
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.btnSecondary}
            disabled={busy}
            onClick={() => {
              setInput([]);
              setRevealIdx(-1);
              setRevealing(true);
              let step = 0;
              const timer = window.setInterval(() => {
                if (step >= target.length) {
                  window.clearInterval(timer);
                  setRevealIdx(-1);
                  setRevealing(false);
                  return;
                }
                setRevealIdx(step);
                step += 1;
              }, STEP_MS);
            }}
          >
            Repetir sequência
          </button>
          <button
            type="button"
            className={styles.btn}
            disabled={busy || revealing || !solved}
            onClick={() => {
              if (!validatePuzzleMoves(seed, "COLOR_SIMON", input)) {
                onMistake?.("CÓDIGO DE CORES CORROMPIDO!");
                return;
              }
              onSubmit(input);
            }}
          >
            Confirmar sequência
          </button>
        </div>
      </div>
    </div>
  );
}
