package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;

public interface MissionEventPublishPort {
  void publishMissionCreated(MissionCreatedKafkaEvent event);
}
