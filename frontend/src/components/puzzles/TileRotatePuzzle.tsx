import { useMemo, useState } from "react";
import { TILE_COUNT, generateTileRotateTarget, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

const ROTATION_SYMBOL = ["↑", "→", "↓", "←"];

export function TileRotatePuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const target = useMemo(() => generateTileRotateTarget(seed), [seed]);
  const [angles, setAngles] = useState<number[]>(() => target.map((v, i) => (v + i + 1) % 4));

  const solved = angles.every((v, i) => v === target[i]);

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Gire as 9 peças até bater no layout do seed. Cada clique gira 90 graus.
      </p>
      <div className={styles.gamePanel}>
        <div className={styles.rotateLegend}>
          <span>Alvo:</span>
          <code>{target.map((v) => ROTATION_SYMBOL[v]).join(" ")}</code>
        </div>
        <div className={styles.rotateGrid}>
          {angles.map((rot, idx) => (
            <button
              key={idx}
              type="button"
              className={`${styles.rotateTile} ${rot === target[idx] ? styles.rotateOk : ""}`}
              disabled={busy}
              onClick={() => {
                setAngles((prev) => {
                  const next = [...prev];
                  next[idx] = (next[idx]! + 1) % 4;
                  return next;
                });
              }}
            >
              <span
                className={styles.rotateArrow}
                style={{ transform: `rotate(${rot * 90}deg)` }}
              >
                ↑
              </span>
            </button>
          ))}
        </div>
        <button
          type="button"
          className={styles.btn}
          disabled={busy || !solved}
          onClick={() => {
            if (!validatePuzzleMoves(seed, "TILE_ROTATE", angles)) {
              onMistake?.("PAINEL FORA DE ALINHAMENTO!");
              return;
            }
            onSubmit(angles);
          }}
        >
          Confirmar rotações
        </button>
      </div>
    </div>
  );
}
