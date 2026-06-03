package br.edu.central.centrallj.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.central.centrallj.domain.PuzzleType;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PuzzleValidationServiceTest {

  private final PuzzleValidationService service = new PuzzleValidationService();

  @Test
  void dragSort_acceptsOrderedSolution() {
    String seed = taskSeed(UUID.randomUUID());
    assertTrue(service.validate(seed, PuzzleType.DRAG_SORT, List.of(0, 1, 2, 3, 4)));
  }

  @Test
  void nodeConnect_acceptsGeneratedSolution() {
    UUID taskId = UUID.fromString("81d32418-828b-4f28-a40c-f52cb31cc972");
    String seed = taskSeed(taskId);
    List<Integer> expected = service.generateExpectedSequence(seed, PuzzleType.NODE_CONNECT);
    assertEquals(5, expected.size());
    assertTrue(service.validate(seed, PuzzleType.NODE_CONNECT, expected));
  }

  @Test
  void sequenceInput_acceptsGeneratedSolution() {
    String seed = taskSeed(UUID.randomUUID());
    List<Integer> expected = service.generateExpectedSequence(seed, PuzzleType.SEQUENCE_INPUT);
    assertTrue(service.validate(seed, PuzzleType.SEQUENCE_INPUT, expected));
  }

  @Test
  void duelStyleSeed_acceptsGeneratedNodeConnect() {
    String base = UUID.randomUUID().toString().replace("-", "");
    String seed = base + "-vil-1";
    List<Integer> expected = service.generateExpectedSequence(seed, PuzzleType.NODE_CONNECT);
    assertTrue(service.validate(seed, PuzzleType.NODE_CONNECT, expected));
  }

  private static String taskSeed(UUID taskId) {
    return taskId.toString().replace("-", "");
  }
}
