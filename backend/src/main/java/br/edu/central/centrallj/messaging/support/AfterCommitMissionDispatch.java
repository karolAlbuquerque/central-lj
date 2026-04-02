package br.edu.central.centrallj.messaging.support;

import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.messaging.producer.MissionCreatedEventProducer;
import br.edu.central.centrallj.service.MissionRealtimeNotifier;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Garante que a publicação no Kafka e o sinal SSE ocorram <strong>após</strong> o commit da transação
 * que persiste a missão — evita inconsistência se a transação falhar após um envio prematuro.
 */
@Component
public class AfterCommitMissionDispatch {

  private final MissionCreatedEventProducer createdEventProducer;
  private final MissionRealtimeNotifier missionRealtimeNotifier;

  public AfterCommitMissionDispatch(
      MissionCreatedEventProducer createdEventProducer,
      MissionRealtimeNotifier missionRealtimeNotifier) {
    this.createdEventProducer = createdEventProducer;
    this.missionRealtimeNotifier = missionRealtimeNotifier;
  }

  public void publishCreatedAndNotifyClients(MissionCreatedKafkaEvent event, UUID missionId) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      createdEventProducer.publish(event);
      missionRealtimeNotifier.notifyMissionUpdate(missionId);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            createdEventProducer.publish(event);
            missionRealtimeNotifier.notifyMissionUpdate(missionId);
          }
        });
  }
}
