package br.edu.central.centrallj.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador Kafka (entrada): recebe {@code missions.created} e delega ingestão.
 */
@Component
public class MissionCreatedConsumer {

  private static final Logger log = LoggerFactory.getLogger(MissionCreatedConsumer.class);

  private final MissionCreatedEventIngestionService ingestionService;

  public MissionCreatedConsumer(MissionCreatedEventIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @KafkaListener(
      id = "centralLjMissionCreated",
      topics = "${central-lj.kafka.topic-created}",
      groupId = "${spring.kafka.consumer.group-id}",
      autoStartup = "${central-lj.kafka.consumer-enabled:true}")
  public void onMissionCreated(String payload) {
    log.debug("[Central-LJ][Kafka] missions.created ← {}", payload);
    ingestionService.ingestJson(payload);
  }
}
