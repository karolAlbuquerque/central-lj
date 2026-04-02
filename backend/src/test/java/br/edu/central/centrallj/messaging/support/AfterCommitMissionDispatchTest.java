package br.edu.central.centrallj.messaging.support;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.messaging.producer.MissionCreatedEventProducer;
import br.edu.central.centrallj.service.MissionRealtimeNotifier;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class AfterCommitMissionDispatchTest {

  @Mock private MissionCreatedEventProducer createdEventProducer;
  @Mock private MissionRealtimeNotifier missionRealtimeNotifier;
  @InjectMocks private AfterCommitMissionDispatch dispatch;

  @Test
  void semTransacaoAtivaPublicaImediatamente() {
    Assertions.assertFalse(TransactionSynchronizationManager.isSynchronizationActive());

    UUID missionId = UUID.randomUUID();
    MissionCreatedKafkaEvent event =
        new MissionCreatedKafkaEvent(
            UUID.randomUUID(),
            "MISSION_CREATED",
            missionId,
            "x",
            TipoAmeaca.TECNOLOGICA,
            PrioridadeMissao.BAIXA,
            MissionStatus.RECEBIDA,
            Instant.now());

    dispatch.publishCreatedAndNotifyClients(event, missionId);

    verify(createdEventProducer).publish(eq(event));
    verify(missionRealtimeNotifier).notifyMissionUpdate(eq(missionId));
  }
}
