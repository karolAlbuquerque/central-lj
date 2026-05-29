import type { PuzzleType } from "../../types/pvp";
import { PUZZLE_LABELS } from "../../utils/puzzle";
import { ArcadeDodgePuzzle } from "./ArcadeDodgePuzzle";
import { ArcadeShooterPuzzle } from "./ArcadeShooterPuzzle";
import { CardMatchPuzzle } from "./CardMatchPuzzle";
import { ColorSimonPuzzle } from "./ColorSimonPuzzle";
import { DragSortPuzzle } from "./DragSortPuzzle";
import { SequenceMemoryPuzzle } from "./SequenceMemoryPuzzle";
import { SlidingTilePuzzle } from "./SlidingTilePuzzle";
import { TerminalHackPuzzle } from "./TerminalHackPuzzle";
import { TileRotatePuzzle } from "./TileRotatePuzzle";
import { WireConnectPuzzle } from "./WireConnectPuzzle";
import styles from "./puzzles.module.css";

type Props = {
  puzzleType: PuzzleType;
  seed: string;
  busy: boolean;
  onMistake?: (message: string) => void;
  onSubmit: (moves: number[]) => void;
};

export function MissionTaskPuzzle({ puzzleType, seed, busy, onMistake, onSubmit }: Props) {
  return (
    <div className={styles.box}>
      <p className={styles.title}>{PUZZLE_LABELS[puzzleType]}</p>
      {puzzleType === "SEQUENCE_INPUT" ? (
        <SequenceMemoryPuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "NODE_CONNECT" ? (
        <WireConnectPuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "DRAG_SORT" ? (
        <DragSortPuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "CARD_MATCH" ? (
        <CardMatchPuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "TILE_ROTATE" ? (
        <TileRotatePuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "COLOR_SIMON" ? (
        <ColorSimonPuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "TERMINAL_HACK" ? (
        <TerminalHackPuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "SLIDING_TILE" ? (
        <SlidingTilePuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "ARCADE_SHOOTER" ? (
        <ArcadeShooterPuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
      {puzzleType === "ARCADE_DODGE" ? (
        <ArcadeDodgePuzzle seed={seed} busy={busy} onMistake={onMistake} onSubmit={onSubmit} />
      ) : null}
    </div>
  );
}
