package br.edu.central.centrallj.messaging.support;

import br.edu.central.centrallj.application.port.out.MissionEventPublishPort;
import br.edu.central.centrallj.application.port.out.MissionNotificationPort;
import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Garante que a publicação no Kafka e o sinal SSE ocorram após o commit da transação
 * que persiste a missão — evita inconsistência se a transação falhar após um envio prematuro.
 */
@Component
public class AfterCommitMissionDispatch {

  private final MissionEventPublishPort missionEventPublishPort;
  private final MissionNotificationPort missionNotificationPort;

  public AfterCommitMissionDispatch(
      MissionEventPublishPort missionEventPublishPort,
      MissionNotificationPort missionNotificationPort) {
    this.missionEventPublishPort = missionEventPublishPort;
    this.missionNotificationPort = missionNotificationPort;
  }

  public void publishCreatedAndNotifyClients(MissionCreatedKafkaEvent event, UUID missionId) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      missionEventPublishPort.publishMissionCreated(event);
      missionNotificationPort.notifyMissionUpdate(missionId);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            missionEventPublishPort.publishMissionCreated(event);
            missionNotificationPort.notifyMissionUpdate(missionId);
          }
        });
  }
}
