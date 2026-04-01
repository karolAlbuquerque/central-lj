package br.edu.central.centrallj.messaging.event;

import br.edu.central.centrallj.domain.Mission;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MissionCreatedEventFactory {

  private static final String TYPE_MISSION_CREATED = "MISSION_CREATED";

  public MissionCreatedKafkaEvent created(Mission mission) {
    return new MissionCreatedKafkaEvent(
        UUID.randomUUID(),
        TYPE_MISSION_CREATED,
        mission.getId(),
        mission.getTitulo(),
        mission.getTipoAmeaca(),
        mission.getPrioridade(),
        mission.getStatus(),
        Instant.now());
  }
}
