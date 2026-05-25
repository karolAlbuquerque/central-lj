package br.edu.central.centrallj.adapter.in.messaging;

import br.edu.central.centrallj.adapter.out.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.application.port.in.ProcessMissionCreatedUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MissionCreatedEventIngestionService {

  public static final String PAYLOAD_TYPE_CREATED = "MISSION_CREATED";

  private static final Logger log = LoggerFactory.getLogger(MissionCreatedEventIngestionService.class);

  private final ObjectMapper objectMapper;
  private final ProcessMissionCreatedUseCase processMissionCreatedUseCase;

  public MissionCreatedEventIngestionService(
      ObjectMapper objectMapper, ProcessMissionCreatedUseCase processMissionCreatedUseCase) {
    this.objectMapper = objectMapper;
    this.processMissionCreatedUseCase = processMissionCreatedUseCase;
  }

  public void ingestJson(String payload) {
    try {
      ingest(objectMapper.readValue(payload, MissionCreatedKafkaEvent.class));
    } catch (JsonProcessingException e) {
      log.error("[Central-LJ][Kafka] Payload inválido em missions.created (JSON)", e);
    }
  }

  public void ingest(MissionCreatedKafkaEvent event) {
    if (event == null || event.missionId() == null) {
      log.warn("[Central-LJ][Kafka] Evento missions.created sem missionId — ignorado.");
      return;
    }
    if (!PAYLOAD_TYPE_CREATED.equals(event.type())) {
      log.warn("[Central-LJ][Kafka] Tipo inesperado em missions.created: {}", event.type());
      return;
    }
    processMissionCreatedUseCase.processAfterCreation(event.missionId());
  }
}
