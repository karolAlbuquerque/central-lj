package br.edu.central.centrallj.adapter.out.messaging;

import br.edu.central.centrallj.adapter.out.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.adapter.out.messaging.producer.MissionCreatedEventProducer;
import br.edu.central.centrallj.application.event.MissionCreatedEvent;
import br.edu.central.centrallj.application.port.out.MissionEventPublishPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KafkaMissionEventPublishAdapter implements MissionEventPublishPort {

  private final MissionCreatedEventProducer producer;

  public KafkaMissionEventPublishAdapter(MissionCreatedEventProducer producer) {
    this.producer = producer;
  }

  @Override
  public void publishMissionCreated(MissionCreatedEvent event) {
    producer.publish(
        new MissionCreatedKafkaEvent(
            UUID.randomUUID(),
            event.type(),
            event.missionId(),
            event.titulo(),
            event.tipoAmeaca(),
            event.prioridade(),
            event.status(),
            event.occurredAt()));
  }
}
