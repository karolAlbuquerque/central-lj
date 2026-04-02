package br.edu.central.centrallj.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer do tópico genérico {@code missions.events} — usado para testes manuais ({@code /api/events/publish-test})
 * e observabilidade. Não participa do workflow de missão; o fluxo principal é {@code missions.created} →
 * {@link MissionCreatedConsumer}.
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
