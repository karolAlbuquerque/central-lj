package br.edu.central.centrallj.messaging.ingestion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.service.MissionWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MissionCreatedEventIngestionServiceTest {

  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Mock private MissionWorkflowService missionWorkflowService;

  private MissionCreatedEventIngestionService ingestionService;

  @BeforeEach
  void setUp() {
    ingestionService = new MissionCreatedEventIngestionService(mapper, missionWorkflowService);
  }

  @Test
  void ingestJsonValidoDelegaWorkflow() throws Exception {
    UUID id = UUID.randomUUID();
    MissionCreatedKafkaEvent event =
        new MissionCreatedKafkaEvent(
            UUID.randomUUID(),
            MissionCreatedEventIngestionService.PAYLOAD_TYPE_CREATED,
            id,
            "t",
            TipoAmeaca.TECNOLOGICA,
            PrioridadeMissao.MEDIA,
            MissionStatus.RECEBIDA,
            Instant.now());
    String json = mapper.writeValueAsString(event);

    ingestionService.ingestJson(json);

    verify(missionWorkflowService).processAfterCreation(eq(id));
  }

  @Test
  void ingestIgnoraTipoDiferente() {
    UUID id = UUID.randomUUID();
    ingestionService.ingest(
        new MissionCreatedKafkaEvent(
            UUID.randomUUID(),
            "OUTRO",
            id,
            null,
            null,
            null,
            null,
            Instant.now()));

    verify(missionWorkflowService, never()).processAfterCreation(any());
  }

  @Test
  void ingestIgnoraMissionIdNulo() {
    ingestionService.ingest(
        new MissionCreatedKafkaEvent(
            UUID.randomUUID(),
            MissionCreatedEventIngestionService.PAYLOAD_TYPE_CREATED,
            null,
            null,
            null,
            null,
            null,
            Instant.now()));

    verify(missionWorkflowService, never()).processAfterCreation(any());
  }
}
