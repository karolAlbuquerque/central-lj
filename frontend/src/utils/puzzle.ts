import type { PuzzleType } from "../types/pvp";

const LCG_A = 1664525;
const LCG_C = 1013904223;
const LCG_M = 0x100000000;

export const PUZZLE_SIZE = 5;
export const CARD_COUNT = 8;
export const TILE_COUNT = 9;
export const SIMON_SIZE = 7;
export const TERMINAL_WORD_COUNT = 3;
export const ARCADE_FRAMES = 16;

export const PUZZLE_LABELS: Record<PuzzleType, string> = {
  SEQUENCE_INPUT: "Memorização de código",
  NODE_CONNECT: "Desarmar fios",
  DRAG_SORT: "Sequência ordenada",
  CARD_MATCH: "Pares de cartas",
  TILE_ROTATE: "Quebra-cabeça de rotação",
  COLOR_SIMON: "Código de cores",
  TERMINAL_HACK: "Hackear terminal",
  SLIDING_TILE: "Sliding tile 3x3",
  ARCADE_SHOOTER: "Arcade: tiro tático",
  ARCADE_DODGE: "Arcade: esquiva orbital"
};

export const WIRE_COLORS = ["#ef4444", "#3b82f6", "#22c55e", "#eab308", "#a855f7"];
export const CARD_SYMBOLS = ["⚡", "🛡️", "🔑", "🎯"];

function lcgNext(state: number): number {
  return ((state * LCG_A + LCG_C) % LCG_M) >>> 0;
}

/** Estado LCG derivado do seed completo (alinhado ao backend). */
function seedToState(seed: string): number {
  let state = 5381;
  for (let i = 0; i < seed.length; i++) {
    state = (((state << 5) + state) + seed.charCodeAt(i)) >>> 0;
  }
  return state >>> 0;
}

/** Mesma ordem de PuzzleType no backend Java. */
export function puzzleTypeFromSeed(seed: string): PuzzleType {
  let state = seedToState(seed);
  state = lcgNext(state);
  const types: PuzzleType[] = [
    "DRAG_SORT",
    "NODE_CONNECT",
    "SEQUENCE_INPUT",
    "CARD_MATCH",
    "TILE_ROTATE",
    "COLOR_SIMON",
    "TERMINAL_HACK",
    "SLIDING_TILE",
    "ARCADE_SHOOTER",
    "ARCADE_DODGE"
  ];
  return types[state % types.length]!;
}

export function validatePuzzleMoves(seed: string, type: PuzzleType, moves: number[]): boolean {
  switch (type) {
    case "DRAG_SORT":
      return (
        moves.length === PUZZLE_SIZE && moves.every((v, i) => v === i)
      );
    case "NODE_CONNECT": {
      const expected = generateNodeConnectSolution(seed, PUZZLE_SIZE);
      return (
        moves.length === PUZZLE_SIZE && moves.every((portId, wireIdx) => portId === expected[wireIdx])
      );
    }
    case "SEQUENCE_INPUT": {
      const expected = generateSequenceInputSequence(seed, PUZZLE_SIZE);
      return moves.length === PUZZLE_SIZE && moves.every((d, i) => d === expected[i]);
    }
    case "CARD_MATCH": {
      const values = generateCardValues(seed);
      return JSON.stringify(moves) === JSON.stringify(buildCardMatchSubmission(values));
    }
    case "TILE_ROTATE": {
      const expected = generateTileRotateTarget(seed);
      return moves.length === TILE_COUNT && moves.every((v, i) => v === expected[i]);
    }
    case "COLOR_SIMON": {
      const expected = generateSimonSequence(seed);
      return moves.length === SIMON_SIZE && moves.every((v, i) => v === expected[i]);
    }
    case "TERMINAL_HACK": {
      const expected = generateTerminalTokens(seed);
      return moves.length === TERMINAL_WORD_COUNT && moves.every((v, i) => v === expected[i]);
    }
    case "SLIDING_TILE": {
      const expected = generateSlidingTarget(seed);
      return moves.length === TILE_COUNT && moves.every((v, i) => v === expected[i]);
    }
    case "ARCADE_SHOOTER": {
      const expected = generateArcadeShooterSequence(seed);
      return moves.length === ARCADE_FRAMES && moves.every((v, i) => v === expected[i]);
    }
    case "ARCADE_DODGE": {
      const expected = generateArcadeDodgeSequence(seed);
      return moves.length === ARCADE_FRAMES && moves.every((v, i) => v === expected[i]);
    }
    default:
      return false;
  }
}

export function generateTileRotateTarget(seed: string): number[] {
  let state = seedToState(seed);
  const result: number[] = [];
  for (let i = 0; i < TILE_COUNT; i++) {
    state = lcgNext(state);
    result.push(state % 4);
  }
  return result;
}

export function generateSimonSequence(seed: string): number[] {
  let state = seedToState(seed);
  const result: number[] = [];
  for (let i = 0; i < SIMON_SIZE; i++) {
    state = lcgNext(state);
    result.push(state % 4);
  }
  return result;
}

export function generateTerminalTokens(seed: string): number[] {
  let state = seedToState(seed);
  const result: number[] = [];
  for (let i = 0; i < TERMINAL_WORD_COUNT; i++) {
    state = lcgNext(state);
    result.push(100 + (state % 900));
  }
  return result;
}

export function generateSlidingTarget(seed: string): number[] {
  let state = seedToState(seed);
  const arr = Array.from({ length: TILE_COUNT }, (_, i) => i);
  for (let i = TILE_COUNT - 1; i > 0; i--) {
    state = lcgNext(state);
    const j = state % (i + 1);
    const tmp = arr[i]!;
    arr[i] = arr[j]!;
    arr[j] = tmp;
  }
  // 3x3: solvable when inversions are even.
  if (!isSlidingSolvable(arr)) {
    const tmp = arr[1]!;
    arr[1] = arr[2]!;
    arr[2] = tmp;
  }
  return arr;
}

function isSlidingSolvable(arr: number[]): boolean {
  let inversions = 0;
  for (let i = 0; i < arr.length; i++) {
    if (arr[i] === 0) continue;
    for (let j = i + 1; j < arr.length; j++) {
      if (arr[j] === 0) continue;
      if (arr[i]! > arr[j]!) inversions += 1;
    }
  }
  return inversions % 2 === 0;
}

export function generateArcadeShooterSequence(seed: string): number[] {
  let state = seedToState(seed);
  const result: number[] = [];
  for (let i = 0; i < ARCADE_FRAMES; i++) {
    state = lcgNext(state);
    result.push(state % 4);
  }
  return result;
}

export function generateArcadeDodgeSequence(seed: string): number[] {
  let state = seedToState(seed);
  const result: number[] = [];
  for (let i = 0; i < ARCADE_FRAMES; i++) {
    state = lcgNext(state);
    result.push(state % 3);
  }
  return result;
}

export function generateDragSortShuffle(seed: string, n: number): number[] {
  let state = seedToState(seed);
  const arr = Array.from({ length: n }, (_, i) => i);
  for (let i = n - 1; i > 0; i--) {
    state = lcgNext(state);
    const j = state % (i + 1);
    const tmp = arr[i];
    arr[i] = arr[j]!;
    arr[j] = tmp;
  }
  return arr;
}

export function generateDragSortTarget(n: number): number[] {
  return Array.from({ length: n }, (_, i) => i);
}

export function generateSequenceInputSequence(seed: string, n: number): number[] {
  let state = seedToState(seed);
  const result: number[] = [];
  for (let i = 0; i < n; i++) {
    state = lcgNext(state);
    result.push(state % 10);
  }
  return result;
}

/** Para cada fio i, índice da porta correta (0..n-1). */
export function generateNodeConnectSolution(seed: string, n: number): number[] {
  let state = seedToState(seed);
  const result: number[] = [];
  const used = new Array(n).fill(false);
  for (let i = 0; i < n; i++) {
    state = lcgNext(state);
    let target: number;
    do {
      state = lcgNext(state);
      target = state % n;
    } while (used[target]);
    used[target] = true;
    result.push(target);
  }
  return result;
}

export function generateNodeConnectDisplayOrder(seed: string, n: number): number[] {
  return generateDragSortShuffle(seed + "ports", n);
}

/**
 * Permutação do tabuleiro: slots[i] = id da carta na posição i.
 * Garante que nenhuma carta permanece na mesma posição da memorização (derangement).
 */
export function generateCardLayoutOrder(seed: string): number[] {
  for (let attempt = 0; attempt < 64; attempt++) {
    const order = generateDragSortShuffle(`${seed}layout-${attempt}`, CARD_COUNT);
    if (order.every((cardId, slot) => cardId !== slot)) {
      return order;
    }
  }
  return Array.from({ length: CARD_COUNT }, (_, i) => (i + 1) % CARD_COUNT);
}

/**
 * Sequência de layouts do embaralhamento: cada passo move cartas de posição
 * (não só “balanço”). O último layout é sempre `finalLayout`.
 */
export function buildCardShufflePath(finalLayout: number[], steps: number): number[][] {
  const path: number[][] = [];
  let current = Array.from({ length: CARD_COUNT }, (_, i) => i);

  const swapAt = (order: number[], a: number, b: number) => {
    const next = [...order];
    const tmp = next[a]!;
    next[a] = next[b]!;
    next[b] = tmp;
    return next;
  };

  const swapsPerStep = 4;

  for (let step = 1; step < steps; step++) {
    for (let k = 0; k < swapsPerStep; k++) {
      const wrongSlots: number[] = [];
      for (let slot = 0; slot < CARD_COUNT; slot++) {
        if (current[slot] !== finalLayout[slot]) wrongSlots.push(slot);
      }

      if (wrongSlots.length >= 2) {
        const a = wrongSlots[k % wrongSlots.length]!;
        const b = wrongSlots[(k + 1) % wrongSlots.length]!;
        current = swapAt(current, a, b);
      } else {
        const a = (step + k) % CARD_COUNT;
        const b = (a + 2) % CARD_COUNT;
        current = swapAt(current, a, b);
      }
    }

    path.push([...current]);
  }

  path.push([...finalLayout]);
  return path;
}

export function generateCardValues(seed: string): number[] {
  let state = seedToState(seed);
  const values: number[] = [];
  for (let pair = 0; pair < 4; pair++) {
    values.push(pair, pair);
  }
  for (let i = CARD_COUNT - 1; i > 0; i--) {
    state = lcgNext(state);
    const j = state % (i + 1);
    const tmp = values[i]!;
    values[i] = values[j]!;
    values[j] = tmp;
  }
  return values;
}

export function buildCardMatchSubmission(values: number[]): number[] {
  const flat: number[] = [];
  const used = new Array(CARD_COUNT).fill(false);
  for (let pair = 0; pair < 4; pair++) {
    let first = -1;
    let second = -1;
    for (let i = 0; i < CARD_COUNT; i++) {
      if (!used[i] && values[i] === pair) {
        if (first < 0) first = i;
        else second = i;
      }
    }
    used[first] = true;
    used[second] = true;
    flat.push(Math.min(first, second), Math.max(first, second));
  }
  return flat;
}
