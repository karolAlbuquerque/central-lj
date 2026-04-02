package br.edu.central.centrallj.messaging.ingestion;

import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.service.MissionWorkflowService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Camada única de interpretação do contrato {@code missions.created}: parse, validação leve do envelope e
 * delegação do processamento de domínio. Mantém o {@linkplain br.edu.central.centrallj.messaging.consumer.MissionCreatedConsumer consumer}
 * como adaptador fino (Kafka → aplicação).
 */
@Service
public class MissionCreatedEventIngestionService {

  public static final String PAYLOAD_TYPE_CREATED = "MISSION_CREATED";

  private static final Logger log = LoggerFactory.getLogger(MissionCreatedEventIngestionService.class);

  private final ObjectMapper objectMapper;
  private final MissionWorkflowService missionWorkflowService;

  public MissionCreatedEventIngestionService(
      ObjectMapper objectMapper, MissionWorkflowService missionWorkflowService) {
    this.objectMapper = objectMapper;
    this.missionWorkflowService = missionWorkflowService;
  }

  /** Entrada principal: payload JSON bruto entregue pelo broker. */
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
    missionWorkflowService.processAfterCreation(event.missionId());
  }
}
