package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.MissionTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionTaskPersistencePort {
  MissionTask save(MissionTask task);
  List<MissionTask> findByMission(UUID missionId);
  Optional<MissionTask> findById(UUID taskId);
}
