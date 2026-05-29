import { useMemo, useState } from "react";
import { TILE_COUNT, generateSlidingTarget, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

const START_BOARD = [1, 2, 3, 4, 5, 6, 7, 8, 0];

function isAdjacent(a: number, b: number): boolean {
  const ar = Math.floor(a / 3);
  const ac = a % 3;
  const br = Math.floor(b / 3);
  const bc = b % 3;
  return Math.abs(ar - br) + Math.abs(ac - bc) === 1;
}

export function SlidingTilePuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const target = useMemo(() => generateSlidingTarget(seed), [seed]);
  const [board, setBoard] = useState<number[]>(START_BOARD);

  const solved = board.every((v, i) => v === target[i]);

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Deslize as peças para montar o layout alvo (0 é o espaço vazio).
      </p>
      <div className={styles.gamePanel}>
        <div className={styles.slidingWrap}>
          <div className={styles.slidingBoard}>
            {board.map((value, idx) => (
              <button
                key={idx}
                type="button"
                className={`${styles.slidingCell} ${value === 0 ? styles.slidingEmpty : ""}`}
                disabled={busy || value === 0}
                onClick={() => {
                  const blank = board.indexOf(0);
                  if (!isAdjacent(idx, blank)) return;
                  const next = [...board];
                  next[blank] = value;
                  next[idx] = 0;
                  setBoard(next);
                }}
              >
                {value === 0 ? "" : value}
              </button>
            ))}
          </div>
          <div className={styles.slidingTarget}>
            {target.map((value, idx) => (
              <span key={idx} className={styles.slidingTargetCell}>
                {value}
              </span>
            ))}
          </div>
        </div>
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.btnSecondary}
            disabled={busy}
            onClick={() => setBoard(START_BOARD)}
          >
            Reiniciar
          </button>
          <button
            type="button"
            className={styles.btn}
            disabled={busy || !solved}
            onClick={() => {
              if (!validatePuzzleMoves(seed, "SLIDING_TILE", board)) {
                onMistake?.("SLIDING TILE INCORRETO!");
                return;
              }
              onSubmit(board);
            }}
          >
            Confirmar layout
          </button>
        </div>
      </div>
    </div>
  );
}
