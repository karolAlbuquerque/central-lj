import {
  useCallback,
  useLayoutEffect,
  useMemo,
  useRef,
  useState,
  type PointerEvent as ReactPointerEvent
} from "react";
import {
  PUZZLE_SIZE,
  WIRE_COLORS,
  generateNodeConnectDisplayOrder,
  validatePuzzleMoves
} from "../../utils/puzzle";
import styles from "./puzzles.module.css";

type Props = {
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

type Point = { x: number; y: number };

type LinePath = { wireIdx: number; d: string; color: string };

function bezierPath(from: Point, to: Point): string {
  const midX = (from.x + to.x) / 2;
  return `M ${from.x} ${from.y} C ${midX} ${from.y}, ${midX} ${to.y}, ${to.x} ${to.y}`;
}

export function WireConnectPuzzle({ seed, busy, onMistake, onSubmit }: Props) {
  const displayOrder = useMemo(() => generateNodeConnectDisplayOrder(seed, PUZZLE_SIZE), [seed]);
  const arenaRef = useRef<HTMLDivElement>(null);
  const wireSocketRefs = useRef<(HTMLSpanElement | null)[]>([]);
  const portSocketRefs = useRef<(HTMLSpanElement | null)[]>([]);

  const [links, setLinks] = useState<(number | null)[]>(() => Array(PUZZLE_SIZE).fill(null));
  const [draggingWire, setDraggingWire] = useState<number | null>(null);
  const [dragPoint, setDragPoint] = useState<Point | null>(null);
  const [hoverPort, setHoverPort] = useState<number | null>(null);
  const [lines, setLines] = useState<LinePath[]>([]);
  const [previewD, setPreviewD] = useState<string | null>(null);

  const allConnected = links.every((v) => v !== null);

  const getLocalPoint = useCallback((clientX: number, clientY: number): Point => {
    const arena = arenaRef.current;
    if (!arena) return { x: 0, y: 0 };
    const rect = arena.getBoundingClientRect();
    return { x: clientX - rect.left, y: clientY - rect.top };
  }, []);

  const socketCenter = useCallback((el: HTMLElement | null): Point | null => {
    const arena = arenaRef.current;
    if (!el || !arena) return null;
    const ar = arena.getBoundingClientRect();
    const er = el.getBoundingClientRect();
    return {
      x: er.left + er.width / 2 - ar.left,
      y: er.top + er.height / 2 - ar.top
    };
  }, []);

  const portVisualIndex = useCallback(
    (portId: number) => displayOrder.findIndex((p) => p === portId),
    [displayOrder]
  );

  const refreshLines = useCallback(() => {
    const next: LinePath[] = [];
    links.forEach((portId, wireIdx) => {
      if (portId === null) return;
      const visualIdx = portVisualIndex(portId);
      if (visualIdx < 0) return;
      const from = socketCenter(wireSocketRefs.current[wireIdx]);
      const to = socketCenter(portSocketRefs.current[visualIdx]);
      if (!from || !to) return;
      next.push({
        wireIdx,
        d: bezierPath(from, to),
        color: WIRE_COLORS[wireIdx] ?? "#fff"
      });
    });
    setLines(next);

    if (draggingWire !== null && dragPoint) {
      const from = socketCenter(wireSocketRefs.current[draggingWire]);
      if (from) setPreviewD(bezierPath(from, dragPoint));
      else setPreviewD(null);
    } else {
      setPreviewD(null);
    }
  }, [links, draggingWire, dragPoint, portVisualIndex, socketCenter]);

  useLayoutEffect(() => {
    refreshLines();
    const arena = arenaRef.current;
    if (!arena) return;
    const ro = new ResizeObserver(() => refreshLines());
    ro.observe(arena);
    return () => ro.disconnect();
  }, [refreshLines]);

  const findPortAt = (clientX: number, clientY: number): number | null => {
    for (let visualIdx = 0; visualIdx < PUZZLE_SIZE; visualIdx++) {
      const el = portSocketRefs.current[visualIdx];
      if (!el) continue;
      const r = el.getBoundingClientRect();
      const pad = 18;
      if (
        clientX >= r.left - pad &&
        clientX <= r.right + pad &&
        clientY >= r.top - pad &&
        clientY <= r.bottom + pad
      ) {
        return visualIdx;
      }
    }
    return null;
  };

  const connectWire = (wireIdx: number, visualIdx: number) => {
    const portId = displayOrder[visualIdx]!;
    if (links.includes(portId) || links[wireIdx] !== null) return;
    const next = [...links];
    next[wireIdx] = portId;
    setLinks(next);
  };

  const onWirePointerDown = (wireIdx: number, e: ReactPointerEvent) => {
    if (busy || links[wireIdx] !== null) return;
    e.preventDefault();
    (e.currentTarget as HTMLElement).setPointerCapture(e.pointerId);
    setDraggingWire(wireIdx);
    setDragPoint(getLocalPoint(e.clientX, e.clientY));
  };

  const onArenaPointerMove = (e: ReactPointerEvent) => {
    if (draggingWire === null) return;
    setDragPoint(getLocalPoint(e.clientX, e.clientY));
    setHoverPort(findPortAt(e.clientX, e.clientY));
  };

  const finishDrag = (clientX: number, clientY: number) => {
    if (draggingWire === null) return;
    const visualIdx = findPortAt(clientX, clientY);
    if (visualIdx !== null) connectWire(draggingWire, visualIdx);
    setDraggingWire(null);
    setDragPoint(null);
    setHoverPort(null);
  };

  const onArenaPointerUp = (e: ReactPointerEvent) => {
    finishDrag(e.clientX, e.clientY);
  };

  const onArenaPointerCancel = () => {
    setDraggingWire(null);
    setDragPoint(null);
    setHoverPort(null);
  };

  return (
    <div className={styles.box}>
      <p className={styles.hint}>
        Arraste cada fio colorido da esquerda até a porta correspondente à direita para desarmar a bomba.
      </p>
      <div className={styles.gamePanel}>
        <div
          ref={arenaRef}
          className={styles.wireArena}
          onPointerMove={onArenaPointerMove}
          onPointerUp={onArenaPointerUp}
          onPointerCancel={onArenaPointerCancel}
        >
          <svg className={styles.wireSvg} aria-hidden>
            {lines.map((line) => (
              <path
                key={line.wireIdx}
                className={styles.wireLine}
                d={line.d}
                stroke={line.color}
              />
            ))}
            {previewD ? (
              <path
                className={`${styles.wireLine} ${styles.wireLinePreview}`}
                d={previewD}
                stroke={draggingWire !== null ? WIRE_COLORS[draggingWire] : "#facc15"}
              />
            ) : null}
          </svg>

          <div className={styles.wireLayout}>
            <div className={styles.wireCol}>
              {Array.from({ length: PUZZLE_SIZE }).map((_, wireIdx) => {
                const connected = links[wireIdx] !== null;
                const dragging = draggingWire === wireIdx;
                return (
                  <div
                    key={wireIdx}
                    className={`${styles.wireNode} ${connected ? styles.wireConnected : ""} ${dragging ? styles.wireDragging : ""}`}
                    onPointerDown={(e) => onWirePointerDown(wireIdx, e)}
                    role="button"
                    tabIndex={connected || busy ? -1 : 0}
                    aria-disabled={busy || connected}
                  >
                    <span className={styles.wireLabel}>Fio {wireIdx + 1}</span>
                    <span
                      className={styles.wireCable}
                      style={{ background: WIRE_COLORS[wireIdx], color: WIRE_COLORS[wireIdx] }}
                    />
                    <span
                      ref={(el) => {
                        wireSocketRefs.current[wireIdx] = el;
                      }}
                      className={`${styles.wireSocket} ${connected ? styles.wireSocketFilled : ""}`}
                      style={
                        connected
                          ? { borderColor: WIRE_COLORS[wireIdx], color: WIRE_COLORS[wireIdx] }
                          : undefined
                      }
                    />
                  </div>
                );
              })}
            </div>

            <div className={styles.bombCore}>
              <span className={styles.bombIcon}>💣</span>
              <span className={styles.bombLabel}>Desarmar</span>
            </div>

            <div className={styles.wireCol}>
              {displayOrder.map((portId, visualIdx) => {
                const taken = links.includes(portId);
                const isTarget = hoverPort === visualIdx && draggingWire !== null && !taken;
                return (
                  <div
                    key={visualIdx}
                    className={`${styles.wireNode} ${styles.portNode} ${taken ? styles.wireConnected : ""} ${isTarget ? styles.portDropTarget : ""}`}
                  >
                    <span
                      ref={(el) => {
                        portSocketRefs.current[visualIdx] = el;
                      }}
                      className={`${styles.wireSocket} ${taken ? styles.wireSocketFilled : ""}`}
                      style={
                        taken
                          ? { borderColor: WIRE_COLORS[portId], color: WIRE_COLORS[portId] }
                          : undefined
                      }
                    />
                    <span
                      className={styles.wireCable}
                      style={{ background: WIRE_COLORS[portId], color: WIRE_COLORS[portId], opacity: 0.35 }}
                    />
                    <span className={`${styles.wireLabel} ${styles.portLabel}`}>
                      Porta {String.fromCharCode(65 + portId)}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        <div className={styles.actions}>
          <button
            type="button"
            className={styles.btnSecondary}
            disabled={busy}
            onClick={() => {
              setLinks(Array(PUZZLE_SIZE).fill(null));
              setDraggingWire(null);
              setDragPoint(null);
              setHoverPort(null);
            }}
          >
            Resetar fios
          </button>
          <button
            type="button"
            className={styles.btn}
            disabled={busy || !allConnected}
            onClick={() => {
              const moves = links as number[];
              if (!validatePuzzleMoves(seed, "NODE_CONNECT", moves)) {
                onMistake?.("BOOOM!");
                return;
              }
              onSubmit(moves);
            }}
          >
            Desarmar bomba
          </button>
        </div>
      </div>
    </div>
  );
}
