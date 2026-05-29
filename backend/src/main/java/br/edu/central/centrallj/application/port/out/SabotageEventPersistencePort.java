package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.SabotageEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SabotageEventPersistencePort {
  SabotageEvent save(SabotageEvent event);
  List<SabotageEvent> findActiveByMission(UUID missionId);
  List<SabotageEvent> findExpiredNotReversed(Instant now);
}
