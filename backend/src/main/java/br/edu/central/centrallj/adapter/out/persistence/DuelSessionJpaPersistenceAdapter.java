package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.entity.DuelSessionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.PlayerMissionEntity;
import br.edu.central.centrallj.adapter.out.persistence.mapper.PvpPersistenceMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.DuelSessionRepository;
import br.edu.central.centrallj.adapter.out.persistence.repository.PlayerMissionRepository;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.port.out.DuelSessionPersistencePort;
import br.edu.central.centrallj.domain.DuelSession;
import br.edu.central.centrallj.domain.DuelStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DuelSessionJpaPersistenceAdapter implements DuelSessionPersistencePort {

  private final DuelSessionRepository duelRepository;
  private final PlayerMissionRepository missionRepository;
  private final PvpPersistenceMapper mapper;

  public DuelSessionJpaPersistenceAdapter(
      DuelSessionRepository duelRepository,
      PlayerMissionRepository missionRepository,
      PvpPersistenceMapper mapper) {
    this.duelRepository = duelRepository;
    this.missionRepository = missionRepository;
    this.mapper = mapper;
  }

  @Override
  public DuelSession save(DuelSession session) {
    PlayerMissionEntity missionEntity =
        missionRepository
            .findById(session.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));

    DuelSessionEntity entity = new DuelSessionEntity();
    entity.setId(session.getId());
    entity.setMission(missionEntity);
    entity.setAttackerUserId(session.getAttackerUserId());
    entity.setDefenderUserId(session.getDefenderUserId());
    entity.setSeed(session.getSeed());
    entity.setPuzzleType(session.getPuzzleType());
    entity.setStatus(session.getStatus());
    entity.setRoundCurrent(session.getRoundCurrent());
    entity.setRoundMax(session.getRoundMax());
    entity.setAttackerRoundsWon(session.getAttackerRoundsWon());
    entity.setDefenderRoundsWon(session.getDefenderRoundsWon());
    entity.setStartedAt(session.getStartedAt());
    entity.setFinishedAt(session.getFinishedAt());
    entity.setTimeoutAt(session.getTimeoutAt());
    entity.setInfiltrationProgress(session.getInfiltrationProgress());

    return mapper.toDomain(duelRepository.save(entity));
  }

  @Override
  public Optional<DuelSession> findById(UUID id) {
    return duelRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<DuelSession> findActiveByMission(UUID missionId, List<DuelStatus> activeStatuses) {
    return duelRepository.findByMissionIdAndStatusIn(missionId, activeStatuses).map(mapper::toDomain);
  }

  @Override
  public boolean hasActiveSession(UUID missionId, List<DuelStatus> activeStatuses) {
    return duelRepository.existsByMissionIdAndStatusIn(missionId, activeStatuses);
  }
}
