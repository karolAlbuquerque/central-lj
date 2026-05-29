package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.PuzzleAttemptEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleAttemptRepository extends JpaRepository<PuzzleAttemptEntity, UUID> {
  List<PuzzleAttemptEntity> findByDuelSessionIdAndUserId(UUID duelSessionId, UUID userId);
  long countByDuelSessionIdAndUserIdAndRoundNumber(UUID duelSessionId, UUID userId, int round);
}
