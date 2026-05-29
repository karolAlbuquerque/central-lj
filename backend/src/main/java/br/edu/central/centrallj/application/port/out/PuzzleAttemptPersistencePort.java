package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.PuzzleAttempt;
import java.util.List;
import java.util.UUID;

public interface PuzzleAttemptPersistencePort {
  PuzzleAttempt save(PuzzleAttempt attempt);
  List<PuzzleAttempt> findByDuelAndUser(UUID duelSessionId, UUID userId);
  long countByDuelUserAndRound(UUID duelSessionId, UUID userId, int round);
}
