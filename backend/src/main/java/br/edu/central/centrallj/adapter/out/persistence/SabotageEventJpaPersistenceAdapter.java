package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.entity.DuelSessionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.PlayerMissionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.SabotageEventEntity;
import br.edu.central.centrallj.adapter.out.persistence.mapper.PvpPersistenceMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.DuelSessionRepository;
import br.edu.central.centrallj.adapter.out.persistence.repository.PlayerMissionRepository;
import br.edu.central.centrallj.adapter.out.persistence.repository.SabotageEventRepository;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.port.out.SabotageEventPersistencePort;
import br.edu.central.centrallj.domain.SabotageEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SabotageEventJpaPersistenceAdapter implements SabotageEventPersistencePort {

  private final SabotageEventRepository eventRepository;
  private final DuelSessionRepository duelRepository;
  private final PlayerMissionRepository missionRepository;
  private final PvpPersistenceMapper mapper;

  public SabotageEventJpaPersistenceAdapter(
      SabotageEventRepository eventRepository,
      DuelSessionRepository duelRepository,
      PlayerMissionRepository missionRepository,
      PvpPersistenceMapper mapper) {
    this.eventRepository = eventRepository;
    this.duelRepository = duelRepository;
    this.missionRepository = missionRepository;
    this.mapper = mapper;
  }

  @Override
  public SabotageEvent save(SabotageEvent event) {
    DuelSessionEntity duelEntity =
        duelRepository
            .findById(event.getDuelSessionId())
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    PlayerMissionEntity missionEntity =
        missionRepository
            .findById(event.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));

    SabotageEventEntity entity = new SabotageEventEntity();
    entity.setId(event.getId());
    entity.setDuelSession(duelEntity);
    entity.setMission(missionEntity);
    entity.setAttackerUserId(event.getAttackerUserId());
    entity.setSabotageType(event.getSabotageType());
    entity.setTargetScope(event.getTargetScope());
    entity.setTargetUserId(event.getTargetUserId());
    entity.setAppliedAt(event.getAppliedAt());
    entity.setExpiresAt(event.getExpiresAt());
    entity.setReversedAt(event.getReversedAt());
    entity.setEffectPayload(event.getEffectPayloadJson());

    return mapper.toDomain(eventRepository.save(entity));
  }

  @Override
  public List<SabotageEvent> findActiveByMission(UUID missionId) {
    return eventRepository
        .findByMissionIdAndReversedAtIsNull(missionId)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<SabotageEvent> findExpiredNotReversed(Instant now) {
    return eventRepository
        .findByReversedAtIsNullAndExpiresAtBefore(now)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }
}
