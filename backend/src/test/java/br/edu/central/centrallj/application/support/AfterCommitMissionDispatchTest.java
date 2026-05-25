package br.edu.central.centrallj.application.support;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.edu.central.centrallj.application.event.MissionCreatedEvent;
import br.edu.central.centrallj.application.port.out.MissionEventPublishPort;
import br.edu.central.centrallj.application.port.out.MissionNotificationPort;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
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

  @Mock private MissionEventPublishPort missionEventPublishPort;
  @Mock private MissionNotificationPort missionNotificationPort;
  @InjectMocks private AfterCommitMissionDispatch dispatch;

  @Test
  void semTransacaoAtivaPublicaImediatamente() {
    Assertions.assertFalse(TransactionSynchronizationManager.isSynchronizationActive());

    UUID missionId = UUID.randomUUID();
    MissionCreatedEvent event =
        new MissionCreatedEvent(
            missionId,
            "MISSION_CREATED",
            Instant.now(),
            "x",
            TipoAmeaca.TECNOLOGICA,
            PrioridadeMissao.BAIXA,
            MissionStatus.RECEBIDA);

    dispatch.publishCreatedAndNotifyClients(event, missionId);

    verify(missionEventPublishPort).publishMissionCreated(eq(event));
    verify(missionNotificationPort).notifyMissionUpdate(eq(missionId));
  }
}
