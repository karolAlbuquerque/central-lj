import { useCallback, useEffect, useState } from "react";
import { PUZZLE_SIZE, generateSequenceInputSequence, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

const REVEAL_MS = 2500;

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

function digitFromKey(key: string): number | null {
  if (key.length === 1 && key >= "0" && key <= "9") return Number(key);
  const numpad = key.match(/^Numpad(\d)$/);
  if (numpad) return Number(numpad[1]);
  const digit = key.match(/^Digit(\d)$/);
  if (digit) return Number(digit[1]);
  return null;
}

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

  const pushDigit = useCallback(
    (digit: number) => {
      if (busy || revealed) return;
      setInput((prev) => (prev.length >= PUZZLE_SIZE ? prev : [...prev, digit]));
    },
    [busy, revealed]
  );

  const popDigit = useCallback(() => {
    if (busy || revealed) return;
    setInput((prev) => (prev.length === 0 ? prev : prev.slice(0, -1)));
  }, [busy, revealed]);

  const submitCode = useCallback(() => {
    if (busy || revealed || input.length !== PUZZLE_SIZE) return;
    if (!validatePuzzleMoves(seed, "SEQUENCE_INPUT", input)) {
      onMistake?.("MEMÓRIA CORROMPIDA!");
      return;
    }
    onSubmit(input);
  }, [busy, revealed, input, seed, onMistake, onSubmit]);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (busy || revealed) return;

      const digit = digitFromKey(e.key);
      if (digit !== null) {
        if (input.length >= PUZZLE_SIZE) return;
        e.preventDefault();
        pushDigit(digit);
        return;
      }

      if (e.key === "Backspace") {
        e.preventDefault();
        popDigit();
        return;
      }

      if (e.key === "Enter" && input.length === PUZZLE_SIZE) {
        e.preventDefault();
        submitCode();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [busy, revealed, input.length, pushDigit, popDigit, submitCode]);

  const showReveal = () => {
    setRevealed(true);
    window.setTimeout(() => setRevealed(false), REVEAL_MS);
  };

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Memorize o código exibido e digite os 5 dígitos na mesma ordem. Use o teclado numérico ou as teclas 0–9;
        Backspace apaga e Enter envia.
      </p>
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
                  onClick={() => pushDigit(digit)}
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
                onClick={popDigit}
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
          onClick={submitCode}
        >
          Enviar código
        </button>
      </div>
    </div>
  );
}
