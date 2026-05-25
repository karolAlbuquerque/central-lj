package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.mapper.PersistenceEntityMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.MissionHistoryRepository;
import br.edu.central.centrallj.application.port.out.MissionHistoryPersistencePort;
import br.edu.central.centrallj.domain.MissionHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MissionHistoryJpaPersistenceAdapter implements MissionHistoryPersistencePort {

  private final MissionHistoryRepository jpa;
  private final PersistenceEntityMapper mapper;

  public MissionHistoryJpaPersistenceAdapter(
      MissionHistoryRepository jpa, PersistenceEntityMapper mapper) {
    this.jpa = jpa;
    this.mapper = mapper;
  }

  @Override
  public MissionHistory save(MissionHistory history) {
    return mapper.toDomain(jpa.save(mapper.toEntity(history)));
  }

  @Override
  public List<MissionHistory> findByMissionIdOrderByOcorridoEmAsc(UUID missionId) {
    return jpa.findByMission_IdOrderByOcorridoEmAsc(missionId).stream().map(mapper::toDomain).toList();
  }
}
