package br.edu.central.centrallj.application.support;

import br.edu.central.centrallj.application.event.MissionCreatedEvent;
import br.edu.central.centrallj.application.port.out.MissionEventPublishPort;
import br.edu.central.centrallj.application.port.out.MissionNotificationPort;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

  public void publishCreatedAndNotifyClients(MissionCreatedEvent event, UUID missionId) {
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
