package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.application.event.MissionCreatedEvent;

public interface MissionEventPublishPort {
  void publishMissionCreated(MissionCreatedEvent event);
}
