package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.domain.PuzzleType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Gera e valida sequências de puzzle a partir de seed determinístico.
 * O mesmo algoritmo LCG deve ser replicado no cliente JavaScript para garantir sequências idênticas.
 * Seed: primeiros 8 hex chars do UUID (sem hífens) → uint32 inicial.
 */
@Service
public class PuzzleValidationService {

  private static final int PUZZLE_SIZE = 5;
  private static final int TILE_SIZE = 9;
  private static final int SIMON_SIZE = 7;
  private static final int TERMINAL_WORD_COUNT = 3;
  private static final int SLIDING_SIZE = 9;
  private static final int ARCADE_FRAMES = 16;
  private static final long LCG_A = 1664525L;
  private static final long LCG_C = 1013904223L;
  private static final long LCG_M = 0xFFFFFFFFL;

  public List<Integer> generateExpectedSequence(String seed, PuzzleType type) {
    long state = seedToState(seed);
    if (type == PuzzleType.DRAG_SORT) {
      return generateDragSortTarget();
    }
    if (type == PuzzleType.NODE_CONNECT) {
      return generateNodeConnect(state);
    }
    if (type == PuzzleType.SEQUENCE_INPUT) {
      return generateSequenceInput(state);
    }
    if (type == PuzzleType.CARD_MATCH) {
      return generateCardMatchPairs(state);
    }
    if (type == PuzzleType.TILE_ROTATE) {
      return generateTileRotate(state);
    }
    if (type == PuzzleType.COLOR_SIMON) {
      return generateColorSimon(state);
    }
    if (type == PuzzleType.TERMINAL_HACK) {
      return generateTerminalHack(state);
    }
    if (type == PuzzleType.SLIDING_TILE) {
      return generateSlidingTile(state);
    }
    if (type == PuzzleType.ARCADE_SHOOTER) {
      return generateArcadeShooter(state);
    }
    if (type == PuzzleType.ARCADE_DODGE) {
      return generateArcadeDodge(state);
    }
    throw new IllegalArgumentException("Tipo de puzzle não suportado: " + type);
  }

  public boolean validate(String seed, PuzzleType type, List<Integer> submitted) {
    List<Integer> expected = generateExpectedSequence(seed, type);
    return expected.equals(submitted);
  }

  private List<Integer> generateDragSortTarget() {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < PUZZLE_SIZE; i++) result.add(i);
    return result;
  }

  private List<Integer> generateNodeConnect(long state) {
    List<Integer> result = new ArrayList<>();
    boolean[] used = new boolean[PUZZLE_SIZE];
    for (int i = 0; i < PUZZLE_SIZE; i++) {
      state = lcgNext(state);
      int target;
      do {
        state = lcgNext(state);
        target = (int) (state % PUZZLE_SIZE);
      } while (used[target]);
      used[target] = true;
      result.add(target);
    }
    return result;
  }

  private List<Integer> generateSequenceInput(long state) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < PUZZLE_SIZE; i++) {
      state = lcgNext(state);
      result.add((int) (state % 10));
    }
    return result;
  }

  /** Pares de índices das cartas: [a,b, c,d, ...] com a < b, ordenados. */
  private List<Integer> generateCardMatchPairs(long state) {
    int[] values = new int[8];
    int idx = 0;
    for (int pair = 0; pair < 4; pair++) {
      values[idx++] = pair;
      values[idx++] = pair;
    }
    for (int i = 7; i > 0; i--) {
      state = lcgNext(state);
      int j = (int) (state % (i + 1));
      int tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
    }
    List<Integer> flat = new ArrayList<>();
    boolean[] used = new boolean[8];
    for (int pair = 0; pair < 4; pair++) {
      int first = -1;
      int second = -1;
      for (int i = 0; i < 8; i++) {
        if (!used[i] && values[i] == pair) {
          if (first < 0) first = i;
          else second = i;
        }
      }
      used[first] = true;
      used[second] = true;
      flat.add(Math.min(first, second));
      flat.add(Math.max(first, second));
    }
    return flat;
  }

  /** 9 rotações finais (0..3), uma por peça do painel 3x3. */
  private List<Integer> generateTileRotate(long state) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < TILE_SIZE; i++) {
      state = lcgNext(state);
      result.add((int) (state % 4));
    }
    return result;
  }

  /** Sequência Simon (botões 0..3). */
  private List<Integer> generateColorSimon(long state) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < SIMON_SIZE; i++) {
      state = lcgNext(state);
      result.add((int) (state % 4));
    }
    return result;
  }

  /** Três "tokens" numéricos (100..999) representados como inteiros. */
  private List<Integer> generateTerminalHack(long state) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < TERMINAL_WORD_COUNT; i++) {
      state = lcgNext(state);
      result.add(100 + (int) (state % 900));
    }
    return result;
  }

  /** Estado final do sliding 3x3 (permutação de 0..8; 0 = vazio). */
  private List<Integer> generateSlidingTile(long state) {
    int[] arr = new int[SLIDING_SIZE];
    for (int i = 0; i < SLIDING_SIZE; i++) {
      arr[i] = i;
    }
    for (int i = SLIDING_SIZE - 1; i > 0; i--) {
      state = lcgNext(state);
      int j = (int) (state % (i + 1));
      int tmp = arr[i];
      arr[i] = arr[j];
      arr[j] = tmp;
    }
    // Garante um estado solucionável para 3x3.
    if (!isSlidingSolvable(arr)) {
      int tmp = arr[1];
      arr[1] = arr[2];
      arr[2] = tmp;
    }
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < SLIDING_SIZE; i++) {
      result.add(arr[i]);
    }
    return result;
  }

  /** Sequência de inputs esperada para shooter (direções 0..3). */
  private List<Integer> generateArcadeShooter(long state) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < ARCADE_FRAMES; i++) {
      state = lcgNext(state);
      result.add((int) (state % 4));
    }
    return result;
  }

  /** Sequência de lanes esperada para dodge (0..2). */
  private List<Integer> generateArcadeDodge(long state) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < ARCADE_FRAMES; i++) {
      state = lcgNext(state);
      result.add((int) (state % 3));
    }
    return result;
  }

  private boolean isSlidingSolvable(int[] arr) {
    int inversions = 0;
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] == 0) continue;
      for (int j = i + 1; j < arr.length; j++) {
        if (arr[j] == 0) continue;
        if (arr[i] > arr[j]) inversions++;
      }
    }
    return inversions % 2 == 0;
  }

  private static long lcgNext(long state) {
    return (state * LCG_A + LCG_C) & LCG_M;
  }

  /** Estado LCG derivado do seed completo (mesmo algoritmo do cliente). */
  private static long seedToState(String seed) {
    long state = 5381L;
    for (int i = 0; i < seed.length(); i++) {
      state = ((state << 5) + state + seed.charAt(i)) & LCG_M;
    }
    return state;
  }
}
