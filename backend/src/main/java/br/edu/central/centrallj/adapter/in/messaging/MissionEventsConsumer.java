package br.edu.central.centrallj.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador Kafka (entrada): tópico genérico {@code missions.events} — testes e observabilidade.
 */
@Component
public class MissionEventsConsumer {

  private static final Logger log = LoggerFactory.getLogger(MissionEventsConsumer.class);

  @KafkaListener(
      id = "centralLjMissionEventsN1",
      topics = "${central-lj.kafka.topic}",
      groupId = "${spring.kafka.consumer.group-id}",
      autoStartup = "${central-lj.kafka.consumer-enabled:true}")
  public void onMissionEvent(String payload) {
    log.debug("[Central-LJ][Kafka] missions.events ← {}", payload);
  }
}
