import { useMemo, useState } from "react";
import { generateTerminalTokens, validatePuzzleMoves } from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

export function TerminalHackPuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const tokens = useMemo(() => generateTerminalTokens(seed), [seed]);
  const [input, setInput] = useState<string[]>(["", "", ""]);

  const parsed = input.map((v) => Number.parseInt(v, 10));
  const complete = parsed.every((v) => Number.isFinite(v));

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Digite os 3 tokens do terminal exatamente como exibidos no painel.
      </p>
      <div className={styles.gamePanel}>
        <div className={styles.terminalTokenPanel}>
          {tokens.map((token, idx) => (
            <div key={idx} className={styles.terminalTokenLine}>
              <span>{`node-${idx + 1}`}</span>
              <strong>{token}</strong>
            </div>
          ))}
        </div>
        <div className={styles.terminalInputRow}>
          {input.map((v, idx) => (
            <input
              key={idx}
              className={styles.terminalInput}
              inputMode="numeric"
              value={v}
              onChange={(e) => {
                const next = [...input];
                next[idx] = e.target.value.replace(/\D/g, "").slice(0, 3);
                setInput(next);
              }}
              placeholder="000"
            />
          ))}
        </div>
        <button
          type="button"
          className={styles.btn}
          disabled={busy || !complete}
          onClick={() => {
            const moves = parsed.map((n) => n || 0);
            if (!validatePuzzleMoves(seed, "TERMINAL_HACK", moves)) {
              onMistake?.("CREDENCIAL TERMINAL INVÁLIDA!");
              return;
            }
            onSubmit(moves);
          }}
        >
          Autenticar terminal
        </button>
      </div>
    </div>
  );
}
