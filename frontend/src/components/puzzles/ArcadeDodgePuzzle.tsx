import { useCallback, useEffect, useMemo, useState } from "react";
import { ARCADE_FRAMES, generateArcadeDodgeSequence, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

const LANE_LABELS = ["Esq", "Centro", "Dir"];

/** 0 = esquerda, 1 = centro, 2 = direita. */
const KEY_TO_LANE: Record<string, number> = {
  ArrowLeft: 0,
  ArrowDown: 1,
  ArrowRight: 2
};

export function ArcadeDodgePuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const target = useMemo(() => generateArcadeDodgeSequence(seed), [seed]);
  const [input, setInput] = useState<number[]>([]);

  const complete = input.length === ARCADE_FRAMES;

  const pushLane = useCallback(
    (lane: number) => {
      if (busy || complete) return;
      setInput((prev) => [...prev, lane]);
    },
    [busy, complete]
  );

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (busy || complete) return;
      const lane = KEY_TO_LANE[e.key];
      if (lane === undefined) return;
      e.preventDefault();
      pushLane(lane);
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [busy, complete, pushLane]);

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Arcade de esquiva: escolha a lane correta em 16 ciclos. Setas: ← esquerda, ↓ centro, → direita.
      </p>
      <div className={styles.gamePanel}>
        <div className={styles.dodgeTimeline}>
          {target.map((lane, idx) => (
            <span
              key={idx}
              className={`${styles.dodgeStep} ${
                idx < input.length ? (input[idx] === lane ? styles.arcadeOk : styles.arcadeMiss) : ""
              }`}
            >
              {lane + 1}
            </span>
          ))}
        </div>
        <div className={styles.dodgeLanes}>
          {LANE_LABELS.map((label, lane) => (
            <button
              key={lane}
              type="button"
              className={styles.arcadeBtn}
              disabled={busy || complete}
              onClick={() => pushLane(lane)}
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
              if (!validatePuzzleMoves(seed, "ARCADE_DODGE", input)) {
                onMistake?.("ESQUIVA COMPROMETIDA!");
                setInput([]);
                return;
              }
              onSubmit(input);
            }}
          >
            Confirmar rota
          </button>
        </div>
      </div>
    </div>
  );
}
