package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.DuelSession;
import br.edu.central.centrallj.domain.DuelStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DuelSessionPersistencePort {
  DuelSession save(DuelSession session);
  Optional<DuelSession> findById(UUID id);
  Optional<DuelSession> findActiveByMission(UUID missionId, List<DuelStatus> activeStatuses);
  boolean hasActiveSession(UUID missionId, List<DuelStatus> activeStatuses);
}
