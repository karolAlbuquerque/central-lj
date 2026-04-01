package br.edu.central.centrallj.messaging.consumer;

import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.service.MissionWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MissionCreatedConsumer {

  private static final Logger log = LoggerFactory.getLogger(MissionCreatedConsumer.class);

  private final ObjectMapper objectMapper;
  private final MissionWorkflowService missionWorkflowService;

  public MissionCreatedConsumer(ObjectMapper objectMapper, MissionWorkflowService missionWorkflowService) {
    this.objectMapper = objectMapper;
    this.missionWorkflowService = missionWorkflowService;
  }

  @KafkaListener(
      id = "centralLjMissionCreated",
      topics = "${central-lj.kafka.topic-created}",
      groupId = "${spring.kafka.consumer.group-id}",
      autoStartup = "${central-lj.kafka.consumer-enabled:true}")
  public void onMissionCreated(String payload) {
    log.debug("[Central-LJ][Kafka] missions.created ← {}", payload);
    try {
      MissionCreatedKafkaEvent event = objectMapper.readValue(payload, MissionCreatedKafkaEvent.class);
      if (!"MISSION_CREATED".equals(event.type())) {
        log.warn("[Central-LJ][Kafka] Tipo inesperado em missions.created: {}", event.type());
        return;
      }
      missionWorkflowService.processAfterCreation(event.missionId());
    } catch (Exception e) {
      log.error("[Central-LJ][Kafka] Falha ao processar missions.created", e);
    }
  }
}
