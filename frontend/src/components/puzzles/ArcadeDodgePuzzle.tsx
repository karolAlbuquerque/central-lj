import { useMemo, useState } from "react";
import { ARCADE_FRAMES, generateArcadeDodgeSequence, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

const LANE_LABELS = ["Esq", "Centro", "Dir"];

export function ArcadeDodgePuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const target = useMemo(() => generateArcadeDodgeSequence(seed), [seed]);
  const [input, setInput] = useState<number[]>([]);

  const complete = input.length === ARCADE_FRAMES;

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Arcade de esquiva: escolha a lane correta em 16 ciclos.
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
              onClick={() => setInput((prev) => [...prev, lane])}
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
