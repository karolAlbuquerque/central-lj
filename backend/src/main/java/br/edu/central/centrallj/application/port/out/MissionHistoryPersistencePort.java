package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.MissionHistory;
import java.util.List;
import java.util.UUID;

public interface MissionHistoryPersistencePort {
  MissionHistory save(MissionHistory history);
  List<MissionHistory> findByMissionIdOrderByOcorridoEmAsc(UUID missionId);
}
