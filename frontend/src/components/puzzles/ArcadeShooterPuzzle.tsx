import { useCallback, useEffect, useMemo, useState } from "react";
import { ARCADE_FRAMES, generateArcadeShooterSequence, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

const ARROWS = ["↑", "→", "↓", "←"];

/** 0 = cima, 1 = direita, 2 = baixo, 3 = esquerda (igual aos botões). */
const KEY_TO_DIRECTION: Record<string, number> = {
  ArrowUp: 0,
  ArrowRight: 1,
  ArrowDown: 2,
  ArrowLeft: 3
};

export function ArcadeShooterPuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const target = useMemo(() => generateArcadeShooterSequence(seed), [seed]);
  const [input, setInput] = useState<number[]>([]);

  const complete = input.length === ARCADE_FRAMES;

  const push = useCallback(
    (direction: number) => {
      if (busy || complete) return;
      setInput((prev) => [...prev, direction]);
    },
    [busy, complete]
  );

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (busy || complete) return;
      const direction = KEY_TO_DIRECTION[e.key];
      if (direction === undefined) return;
      e.preventDefault();
      push(direction);
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [busy, complete, push]);

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Arcade de tiro: grave 16 comandos de mira na ordem certa. Use os botões ou as setas do teclado.
      </p>
      <div className={styles.gamePanel}>
        <div className={styles.arcadeTrack}>
          {target.map((v, i) => (
            <span
              key={i}
              className={`${styles.arcadeStep} ${
                i < input.length ? (input[i] === v ? styles.arcadeOk : styles.arcadeMiss) : ""
              }`}
            >
              {ARROWS[v]}
            </span>
          ))}
        </div>
        <div className={styles.arcadePad}>
          {ARROWS.map((label, idx) => (
            <button
              key={idx}
              type="button"
              className={styles.arcadeBtn}
              disabled={busy || complete}
              onClick={() => push(idx)}
            >
              {label}
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
            Apagar último
          </button>
          <button
            type="button"
            className={styles.btn}
            disabled={busy || !complete}
            onClick={() => {
              if (!validatePuzzleMoves(seed, "ARCADE_SHOOTER", input)) {
                onMistake?.("MIRA TÁTICA FALHOU!");
                setInput([]);
                return;
              }
              onSubmit(input);
            }}
          >
            Disparar sequência
          </button>
        </div>
      </div>
    </div>
  );
}
