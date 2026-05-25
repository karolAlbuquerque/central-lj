package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.application.port.out.MissionHistoryPersistencePort;
import br.edu.central.centrallj.domain.MissionHistory;
import br.edu.central.centrallj.repository.MissionHistoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MissionHistoryJpaPersistenceAdapter implements MissionHistoryPersistencePort {

  private final MissionHistoryRepository jpa;

  public MissionHistoryJpaPersistenceAdapter(MissionHistoryRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public MissionHistory save(MissionHistory history) {
    return jpa.save(history);
  }

  @Override
  public List<MissionHistory> findByMissionIdOrderByOcorridoEmAsc(UUID missionId) {
    return jpa.findByMission_IdOrderByOcorridoEmAsc(missionId);
  }
}
