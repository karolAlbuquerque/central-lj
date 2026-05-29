import { useCallback, useEffect, useMemo, useRef, useState, type CSSProperties } from "react";
import {
  buildCardMatchSubmission,
  buildCardShufflePath,
  CARD_COUNT,
  CARD_SYMBOLS,
  generateCardLayoutOrder,
  generateCardValues
} from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

type Phase = "reveal" | "hide" | "shuffle" | "play";

const PAIR_COUNT = CARD_COUNT / 2;
const GRID_COLS = 4;
const GRID_ROWS = CARD_COUNT / GRID_COLS;
const REVEAL_MS = 2800;
const HIDE_MS = 800;
const SHUFFLE_MS = 2800;
const SHUFFLE_STEPS = 8;
const FACE_UP_MS = 2200;

const CARD_LABELS = ["Alpha", "Beta", "Gamma", "Delta"];

const CARD_WIDTH_PX = 92;
const CARD_HEIGHT_PX = Math.round((CARD_WIDTH_PX * 7) / 5);
const CARD_GAP_PX = 14;
const BOARD_PAD_PX = 18;

const BOARD_WIDTH_PX =
  BOARD_PAD_PX * 2 + GRID_COLS * CARD_WIDTH_PX + (GRID_COLS - 1) * CARD_GAP_PX;
const BOARD_HEIGHT_PX =
  BOARD_PAD_PX * 2 + GRID_ROWS * CARD_HEIGHT_PX + (GRID_ROWS - 1) * CARD_GAP_PX;

const boardStyle: CSSProperties = {
  width: BOARD_WIDTH_PX,
  height: BOARD_HEIGHT_PX,
  maxWidth: "100%"
};

const INITIAL_SLOTS = Array.from({ length: CARD_COUNT }, (_, i) => i);

function slotStyle(slotIndex: number): CSSProperties {
  const col = slotIndex % GRID_COLS;
  const row = Math.floor(slotIndex / GRID_COLS);
  return {
    left: BOARD_PAD_PX + col * (CARD_WIDTH_PX + CARD_GAP_PX),
    top: BOARD_PAD_PX + row * (CARD_HEIGHT_PX + CARD_GAP_PX),
    width: CARD_WIDTH_PX,
    height: CARD_HEIGHT_PX
  };
}

function PlayingCardBack({ patternId }: { patternId: string }) {
  const hatchRef = `cardHatch-${patternId}`;
  return (
    <span className={styles.playingCardBack} aria-hidden>
      <span className={styles.playingCardBackEdge}>
        <span className={styles.playingCardBackField}>
          <svg className={styles.playingCardBackSvg} viewBox="0 0 100 140" aria-hidden>
            <defs>
              <pattern
                id={hatchRef}
                width="8"
                height="8"
                patternUnits="userSpaceOnUse"
                patternTransform="rotate(45)"
              >
                <line x1="0" y1="0" x2="0" y2="8" stroke="rgba(255,255,255,0.07)" strokeWidth="2" />
              </pattern>
            </defs>
            <rect width="100" height="140" fill="#1a3a8a" />
            <rect width="100" height="140" fill={`url(#${hatchRef})`} />
            <ellipse cx="50" cy="42" rx="26" ry="20" fill="none" stroke="rgba(255,255,255,0.32)" strokeWidth="1.2" />
            <ellipse cx="50" cy="98" rx="26" ry="20" fill="none" stroke="rgba(255,255,255,0.32)" strokeWidth="1.2" />
            <ellipse cx="50" cy="42" rx="16" ry="12" fill="none" stroke="rgba(255,255,255,0.2)" strokeWidth="0.8" />
            <ellipse cx="50" cy="98" rx="16" ry="12" fill="none" stroke="rgba(255,255,255,0.2)" strokeWidth="0.8" />
            <circle cx="50" cy="42" r="5" fill="rgba(255,255,255,0.12)" />
            <circle cx="50" cy="98" r="5" fill="rgba(255,255,255,0.12)" />
            <path
              d="M50 28 L54 36 L62 36 L56 42 L58 50 L50 46 L42 50 L44 42 L38 36 L46 36 Z"
              fill="rgba(255,255,255,0.18)"
            />
            <path
              d="M50 84 L54 92 L62 92 L56 98 L58 106 L50 102 L42 106 L44 98 L38 92 L46 92 Z"
              fill="rgba(255,255,255,0.18)"
            />
          </svg>
        </span>
      </span>
    </span>
  );
}

export function CardMatchPuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const values = useMemo(() => generateCardValues(seed), [seed]);
  const finalLayout = useMemo(() => generateCardLayoutOrder(seed), [seed]);
  const shufflePath = useMemo(
    () => buildCardShufflePath(finalLayout, SHUFFLE_STEPS),
    [finalLayout]
  );

  const [phase, setPhase] = useState<Phase>("reveal");
  const [slots, setSlots] = useState<number[]>(() => [...INITIAL_SLOTS]);
  /** Cartas viradas aguardando segunda escolha ou timer de olhadinha. */
  const [faceUp, setFaceUp] = useState<number[]>([]);
  const [matched, setMatched] = useState<Set<number>>(new Set());
  const [lock, setLock] = useState(false);
  const submitted = useRef(false);
  const [version, setVersion] = useState(0);
  const [mismatchHits, setMismatchHits] = useState(0);
  const [hitFx, setHitFx] = useState<"ok" | "miss" | null>(null);

  const hideTimerRef = useRef<number | null>(null);

  const pairsFound = matched.size / 2;
  const canPlay = phase === "play" && !busy && !lock;

  const clearHideTimer = () => {
    if (hideTimerRef.current) {
      window.clearTimeout(hideTimerRef.current);
      hideTimerRef.current = null;
    }
  };

  const runIntroSequence = useCallback(() => {
    setPhase("reveal");
    setSlots([...INITIAL_SLOTS]);
    setFaceUp([]);
    setMatched(new Set());
    setLock(false);
    setMismatchHits(0);
    setHitFx(null);
    submitted.current = false;
    clearHideTimer();

    const t1 = window.setTimeout(() => setPhase("hide"), REVEAL_MS);
    const t2 = window.setTimeout(() => setPhase("shuffle"), REVEAL_MS + HIDE_MS);
    const t3 = window.setTimeout(() => setPhase("play"), REVEAL_MS + HIDE_MS + SHUFFLE_MS);

    return () => {
      window.clearTimeout(t1);
      window.clearTimeout(t2);
      window.clearTimeout(t3);
    };
  }, [seed]);

  useEffect(() => {
    if (phase !== "shuffle") return undefined;

    const stepMs = Math.floor(SHUFFLE_MS / SHUFFLE_STEPS);
    let step = 0;

    const tick = window.setInterval(() => {
      if (step < shufflePath.length) {
        setSlots(shufflePath[step]!);
        step += 1;
      } else {
        window.clearInterval(tick);
      }
    }, stepMs);

    return () => window.clearInterval(tick);
  }, [phase, shufflePath]);

  useEffect(() => {
    const cleanup = runIntroSequence();
    return () => {
      cleanup();
      clearHideTimer();
    };
  }, [runIntroSequence, version]);

  const scheduleHideSingle = (cardId: number) => {
    clearHideTimer();
    hideTimerRef.current = window.setTimeout(() => {
      setFaceUp((prev) => prev.filter((id) => id !== cardId));
      hideTimerRef.current = null;
    }, FACE_UP_MS);
  };

  const resolvePair = (a: number, b: number) => {
    setLock(true);
    setFaceUp([a, b]);

    if (values[a] === values[b]) {
      const newMatched = new Set(matched);
      newMatched.add(a);
      newMatched.add(b);
      setMatched(newMatched);
      setFaceUp([]);
      setLock(false);
      if (newMatched.size === CARD_COUNT && !submitted.current) {
        submitted.current = true;
        onSubmit(buildCardMatchSubmission(values));
      }
      setHitFx("ok");
      window.setTimeout(() => setHitFx(null), 380);
    } else {
      setMismatchHits((n) => n + 1);
      onMistake?.("PARES FALHARAM!");
      setHitFx("miss");
      window.setTimeout(() => {
        setFaceUp([]);
        setLock(false);
        setHitFx(null);
      }, 700);
    }
  };

  const onCardClick = (cardId: number) => {
    if (!canPlay || matched.has(cardId) || faceUp.includes(cardId)) return;

    if (faceUp.length === 0) {
      setFaceUp([cardId]);
      scheduleHideSingle(cardId);
      return;
    }

    if (faceUp.length === 1) {
      const first = faceUp[0]!;
      if (first === cardId) return;
      clearHideTimer();
      resolvePair(first, cardId);
    }
  };

  const isFaceUp = (cardId: number) =>
    phase === "reveal" || matched.has(cardId) || faceUp.includes(cardId);

  const phaseLabel =
    phase === "reveal"
      ? "Memorize todas as cartas…"
      : phase === "hide"
        ? "Virando cartas…"
        : phase === "shuffle"
          ? "Embaralhando — todas as cartas mudam de lugar…"
          : faceUp.length === 1
            ? "Olhadinha: clique outra carta para tentar o par"
            : "Clique numa carta — ela vira e fecha sozinha";

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        <strong>Clique</strong> numa carta: ela vira por alguns segundos e fecha. Enquanto estiver virada,
        clique em outra para tentar o par. No embaralhamento, todas as posições mudam.
      </p>
      <div className={styles.gamePanel}>
        <div
          className={`${styles.cardArena} ${hitFx === "miss" ? styles.cardArenaMiss : ""} ${
            phase === "shuffle" ? styles.cardArenaShuffle : ""
          } ${faceUp.length === 1 ? styles.cardArenaPeeking : ""}`}
        >
          <p
            className={`${styles.cardPhaseBanner} ${
              phase !== "play" || faceUp.length > 0 ? styles.cardPhaseBannerActive : ""
            }`}
          >
            {phaseLabel}
          </p>

          <div className={styles.progressPills}>
            {Array.from({ length: PAIR_COUNT }).map((_, i) => (
              <span key={i} className={`${styles.progressPill} ${i < pairsFound ? styles.done : ""}`} />
            ))}
          </div>

          <div className={styles.cardHud}>
            <span className={styles.cardHudItem}>
              Pares: {pairsFound}/{PAIR_COUNT}
            </span>
            <span className={`${styles.cardHudItem} ${styles.cardHudMiss}`}>
              Erros: {mismatchHits}
            </span>
          </div>

          <div
            className={`${styles.cardGridBoard} ${phase === "shuffle" ? styles.cardGridShuffling : ""}`}
            style={boardStyle}
          >
            {Array.from({ length: CARD_COUNT }, (_, cardId) => {
              const slotIndex = slots.indexOf(cardId);
              if (slotIndex < 0) return null;
              const show = isFaceUp(cardId);
              const symbol = values[cardId]!;
              const pairLabel = CARD_LABELS[symbol] ?? "?";

              return (
                <button
                  key={`${version}-card-${cardId}`}
                  type="button"
                  className={`${styles.memoryCardAbs} ${matched.has(cardId) ? styles.matched : ""} ${
                    hitFx === "miss" && faceUp.includes(cardId) ? styles.missPulse : ""
                  } ${hitFx === "ok" && matched.has(cardId) ? styles.matchPulse : ""} ${
                    faceUp.length === 1 && faceUp[0] === cardId ? styles.memoryCardPeeking : ""
                  } ${faceUp.includes(cardId) ? styles.memoryCardSelected : ""}`}
                  style={slotStyle(slotIndex)}
                  disabled={busy || matched.has(cardId) || phase !== "play" || lock}
                  onClick={() => onCardClick(cardId)}
                >
                  <span className={styles.memoryCardLift}>
                    <span
                      className={`${styles.flipCardInner} ${show ? styles.flipCardInnerFlipped : ""}`}
                    >
                      <span className={`${styles.flipFace} ${styles.flipFront}`}>
                        <PlayingCardBack patternId={`${version}-${cardId}`} />
                      </span>
                      <span
                        className={`${styles.flipFace} ${styles.flipBack} ${styles[`cardFace${symbol}`]}`}
                      >
                        <span className={styles.cardCornerTL}>
                          <span className={styles.cardCornerSymbol}>{CARD_SYMBOLS[symbol]}</span>
                          <span className={styles.cardCornerRank}>{pairLabel.charAt(0)}</span>
                        </span>
                        <span className={styles.cardCornerBR}>
                          <span className={styles.cardCornerSymbol}>{CARD_SYMBOLS[symbol]}</span>
                          <span className={styles.cardCornerRank}>{pairLabel.charAt(0)}</span>
                        </span>
                        <span className={styles.cardSymbolWrap}>
                          <span className={styles.cardSymbol}>{CARD_SYMBOLS[symbol]}</span>
                        </span>
                      </span>
                    </span>
                  </span>
                </button>
              );
            })}
            {phase === "shuffle" ? <div className={styles.shuffleOverlay}>Embaralhando…</div> : null}
          </div>

          <button
            type="button"
            className={styles.btnSecondary}
            disabled={busy || lock}
            onClick={() => {
              clearHideTimer();
              setVersion((v) => v + 1);
            }}
          >
            Reiniciar cartas
          </button>
        </div>
      </div>
    </div>
  );
}
