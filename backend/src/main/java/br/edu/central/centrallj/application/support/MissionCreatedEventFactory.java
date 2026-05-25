package br.edu.central.centrallj.application.support;

import br.edu.central.centrallj.application.event.MissionCreatedEvent;
import br.edu.central.centrallj.domain.Mission;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class MissionCreatedEventFactory {

  private static final String TYPE_MISSION_CREATED = "MISSION_CREATED";

  public MissionCreatedEvent created(Mission mission) {
    return new MissionCreatedEvent(
        mission.getId(),
        TYPE_MISSION_CREATED,
        Instant.now(),
        mission.getTitulo(),
        mission.getTipoAmeaca(),
        mission.getPrioridade(),
        mission.getStatus());
  }
}
