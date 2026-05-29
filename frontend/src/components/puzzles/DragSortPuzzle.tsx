import { useMemo, useState } from "react";
import {
  PUZZLE_SIZE,
  generateDragSortShuffle,
  generateDragSortTarget,
  validatePuzzleMoves
} from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

export function DragSortPuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const target = useMemo(() => generateDragSortTarget(PUZZLE_SIZE), []);
  const [order, setOrder] = useState<number[]>(() => generateDragSortShuffle(seed, PUZZLE_SIZE));
  const [dragIdx, setDragIdx] = useState<number | null>(null);
  const [overIdx, setOverIdx] = useState<number | null>(null);

  const isSorted = order.every((v, i) => v === target[i]);

  const moveChip = (from: number, to: number) => {
    if (from === to) return;
    const next = [...order];
    const [item] = next.splice(from, 1);
    next.splice(to, 0, item!);
    setOrder(next);
  };

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Arraste os blocos para ordená-los de 1 a 5. Solte sobre outro bloco para reposicionar.
      </p>
      <div className={styles.gamePanel}>
        <div className={styles.sortRack}>
          <span className={styles.sortHint}>Trilho de sequência</span>
          <div className={styles.sortTrack}>
            {order.map((val, idx) => {
              const inPlace = val === target[idx];
              return (
                <div
                  key={`${idx}-${val}`}
                  className={`${styles.sortChip} ${dragIdx === idx ? styles.dragging : ""} ${overIdx === idx ? styles.dragOver : ""} ${inPlace ? styles.correctGlow : ""}`}
                  draggable={!busy}
                  onDragStart={() => setDragIdx(idx)}
                  onDragEnd={() => {
                    setDragIdx(null);
                    setOverIdx(null);
                  }}
                  onDragOver={(e) => {
                    e.preventDefault();
                    if (dragIdx !== null && dragIdx !== idx) setOverIdx(idx);
                  }}
                  onDragLeave={() => setOverIdx(null)}
                  onDrop={(e) => {
                    e.preventDefault();
                    if (dragIdx !== null) moveChip(dragIdx, idx);
                    setDragIdx(null);
                    setOverIdx(null);
                  }}
                >
                  {val + 1}
                </div>
              );
            })}
          </div>
        </div>
        <button
          type="button"
          className={styles.btn}
          disabled={busy || !isSorted}
          onClick={() => {
            if (!validatePuzzleMoves(seed, "DRAG_SORT", order)) {
              onMistake?.("ORDEM QUEBRADA!");
              return;
            }
            onSubmit(order);
          }}
        >
          Confirmar ordem
        </button>
      </div>
    </div>
  );
}
