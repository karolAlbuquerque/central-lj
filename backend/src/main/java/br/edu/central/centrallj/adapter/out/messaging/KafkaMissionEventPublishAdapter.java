package br.edu.central.centrallj.adapter.out.messaging;

import br.edu.central.centrallj.application.port.out.MissionEventPublishPort;
import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.messaging.producer.MissionCreatedEventProducer;
import org.springframework.stereotype.Component;

@Component
public class KafkaMissionEventPublishAdapter implements MissionEventPublishPort {

  private final MissionCreatedEventProducer producer;

  public KafkaMissionEventPublishAdapter(MissionCreatedEventProducer producer) {
    this.producer = producer;
  }

  @Override
  public void publishMissionCreated(MissionCreatedKafkaEvent event) {
    producer.publish(event);
  }
}
