import { useEffect, useState } from "react";
import { PUZZLE_SIZE, generateSequenceInputSequence, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

const REVEAL_MS = 2500;

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

export function SequenceMemoryPuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const [target, setTarget] = useState<number[]>([]);
  const [input, setInput] = useState<number[]>([]);
  const [revealed, setRevealed] = useState(false);

  useEffect(() => {
    setTarget(generateSequenceInputSequence(seed, PUZZLE_SIZE));
    setInput([]);
    setRevealed(true);
    const t = window.setTimeout(() => setRevealed(false), REVEAL_MS);
    return () => window.clearTimeout(t);
  }, [seed]);

  const showReveal = () => {
    setRevealed(true);
    window.setTimeout(() => setRevealed(false), REVEAL_MS);
  };

  return (
    <div className={styles.box}>
      <p className={styles.hint}>Memorize o código exibido e digite os 5 dígitos na mesma ordem.</p>
      <div className={styles.gamePanel}>
        {revealed ? (
          <div className={styles.terminalReveal}>
            <div className={styles.terminalDigits}>
              {target.map((d, i) => (
                <span key={i}>{d}</span>
              ))}
            </div>
          </div>
        ) : (
          <>
            <div className={styles.slotRow}>
              {Array.from({ length: PUZZLE_SIZE }).map((_, idx) => (
                <span
                  key={idx}
                  className={`${styles.codeSlot} ${input[idx] !== undefined ? styles.filled : ""}`}
                >
                  {input[idx] ?? "·"}
                </span>
              ))}
            </div>
            <div className={styles.keypad}>
              {Array.from({ length: 10 }).map((_, digit) => (
                <button
                  key={digit}
                  type="button"
                  className={styles.keypadBtn}
                  disabled={busy || input.length >= PUZZLE_SIZE}
                  onClick={() =>
                    setInput((prev) => (prev.length >= PUZZLE_SIZE ? prev : [...prev, digit]))
                  }
                >
                  {digit}
                </button>
              ))}
            </div>
            <div className={styles.actions}>
              <button
                type="button"
                className={styles.btnSecondary}
                disabled={busy || input.length === 0}
                onClick={() => setInput((prev) => prev.slice(0, -1))}
              >
                Apagar
              </button>
              <button type="button" className={styles.btnSecondary} disabled={busy} onClick={showReveal}>
                Rever código
              </button>
            </div>
          </>
        )}
        <button
          type="button"
          className={styles.btn}
          disabled={busy || revealed || input.length !== PUZZLE_SIZE}
          onClick={() => {
            if (!validatePuzzleMoves(seed, "SEQUENCE_INPUT", input)) {
              onMistake?.("MEMÓRIA CORROMPIDA!");
              return;
            }
            onSubmit(input);
          }}
        >
          Enviar código
        </button>
      </div>
    </div>
  );
}
