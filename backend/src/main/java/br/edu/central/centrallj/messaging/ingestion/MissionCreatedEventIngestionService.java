package br.edu.central.centrallj.messaging.ingestion;

import br.edu.central.centrallj.application.port.in.missions.ProcessMissionAfterCreationUseCase;
import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
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
  private final ProcessMissionAfterCreationUseCase processAfterCreation;

  public MissionCreatedEventIngestionService(
      ObjectMapper objectMapper, ProcessMissionAfterCreationUseCase processAfterCreation) {
    this.objectMapper = objectMapper;
    this.processAfterCreation = processAfterCreation;
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
    processAfterCreation.processAfterCreation(event.missionId());
  }
}
