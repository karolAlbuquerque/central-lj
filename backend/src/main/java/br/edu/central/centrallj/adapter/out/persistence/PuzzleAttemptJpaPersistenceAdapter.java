package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.entity.DuelSessionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.PuzzleAttemptEntity;
import br.edu.central.centrallj.adapter.out.persistence.mapper.PvpPersistenceMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.DuelSessionRepository;
import br.edu.central.centrallj.adapter.out.persistence.repository.PuzzleAttemptRepository;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.port.out.PuzzleAttemptPersistencePort;
import br.edu.central.centrallj.domain.PuzzleAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PuzzleAttemptJpaPersistenceAdapter implements PuzzleAttemptPersistencePort {

  private final PuzzleAttemptRepository attemptRepository;
  private final DuelSessionRepository duelRepository;
  private final PvpPersistenceMapper mapper;

  public PuzzleAttemptJpaPersistenceAdapter(
      PuzzleAttemptRepository attemptRepository,
      DuelSessionRepository duelRepository,
      PvpPersistenceMapper mapper) {
    this.attemptRepository = attemptRepository;
    this.duelRepository = duelRepository;
    this.mapper = mapper;
  }

  @Override
  public PuzzleAttempt save(PuzzleAttempt attempt) {
    DuelSessionEntity duelEntity =
        duelRepository
            .findById(attempt.getDuelSessionId())
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));

    PuzzleAttemptEntity entity = new PuzzleAttemptEntity();
    entity.setId(attempt.getId());
    entity.setDuelSession(duelEntity);
    entity.setUserId(attempt.getUserId());
    entity.setRoundNumber(attempt.getRoundNumber());
    entity.setMoveSequence(attempt.getMoveSequenceJson());
    entity.setValid(attempt.getValid());
    entity.setTimeMs(attempt.getTimeMs());
    entity.setSubmittedAt(attempt.getSubmittedAt());

    return mapper.toDomain(attemptRepository.save(entity));
  }

  @Override
  public List<PuzzleAttempt> findByDuelAndUser(UUID duelSessionId, UUID userId) {
    return attemptRepository
        .findByDuelSessionIdAndUserId(duelSessionId, userId)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public long countByDuelUserAndRound(UUID duelSessionId, UUID userId, int round) {
    return attemptRepository.countByDuelSessionIdAndUserIdAndRoundNumber(duelSessionId, userId, round);
  }
}
