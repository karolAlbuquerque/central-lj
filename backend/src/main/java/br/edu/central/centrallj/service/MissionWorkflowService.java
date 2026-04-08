package br.edu.central.centrallj.service;

import br.edu.central.centrallj.application.port.in.missions.ProcessMissionAfterCreationUseCase;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.repository.MissionRepository;
import br.edu.central.centrallj.service.workflow.MissionProcessingFlowStrategy;
import br.edu.central.centrallj.service.workflow.MissionProcessingFlowStrategyResolver;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MissionWorkflowService implements ProcessMissionAfterCreationUseCase {

  private static final Logger log = LoggerFactory.getLogger(MissionWorkflowService.class);

  private final MissionRepository missionRepository;
  private final MissionProcessingFlowStrategyResolver strategyResolver;
  private final TransactionTemplate transactionTemplate;
  private final MissionHistoryRecorder missionHistoryRecorder;
  private final MissionRealtimeNotifier missionRealtimeNotifier;
  private final int stepDelayMs;

  public MissionWorkflowService(
      MissionRepository missionRepository,
      MissionProcessingFlowStrategyResolver strategyResolver,
      PlatformTransactionManager transactionManager,
      MissionHistoryRecorder missionHistoryRecorder,
      MissionRealtimeNotifier missionRealtimeNotifier,
      @Value("${central-lj.kafka.workflow-step-delay-ms:200}") int stepDelayMs) {
    this.missionRepository = missionRepository;
    this.strategyResolver = strategyResolver;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.missionHistoryRecorder = missionHistoryRecorder;
    this.missionRealtimeNotifier = missionRealtimeNotifier;
    this.stepDelayMs = stepDelayMs;
  }

  /**
   * Executado pelo consumer Kafka: evolui a missão a partir de {@link MissionStatus#RECEBIDA}. Idempotente
   * se o status já tiver avançado (reentrega de mensagem).
   */
  @Override
  public void processAfterCreation(UUID missionId) {
    Mission initial = missionRepository.findById(missionId).orElse(null);
    if (initial == null) {
      log.warn("[Central-LJ][Workflow] Missão {} não encontrada.", missionId);
      return;
    }
    if (initial.getStatus() != MissionStatus.RECEBIDA) {
      log.debug("[Central-LJ][Workflow] Ignorado {} — status já {}", missionId, initial.getStatus());
      return;
    }

    MissionProcessingFlowStrategy strategy = strategyResolver.resolve(initial);

    try {
      for (MissionStatus next : strategy.orderedStepsAfterRecebida()) {
        sleepStep();
        transactionTemplate.executeWithoutResult(
            status -> {
              Mission m =
                  missionRepository
                      .findById(missionId)
                      .orElseThrow(
                          () -> new IllegalStateException("Missão ausente durante workflow: " + missionId));
              if (m.getStatus() == MissionStatus.FALHA_PROCESSAMENTO
                  || m.getStatus() == MissionStatus.CONCLUIDA) {
                return;
              }
              MissionStatus prev = m.getStatus();
              m.setStatus(next);
              m.setUltimaAtualizacao(Instant.now());
              missionRepository.save(m);
              missionHistoryRecorder.record(
                  m,
                  prev,
                  next,
                  "Transição automática pelo pipeline (consumer Kafka).",
                  MissionHistoryOrigin.KAFKA_WORKFLOW);
              log.info("[Central-LJ][Workflow] Missão {} → {}", missionId, next);
            });
        missionRealtimeNotifier.notifyMissionUpdate(missionId);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      markFailure(missionId);
    } catch (Exception e) {
      log.error("[Central-LJ][Workflow] Erro processando missão {}", missionId, e);
      markFailure(missionId);
    }
  }

  private void sleepStep() throws InterruptedException {
    if (stepDelayMs > 0) {
      Thread.sleep(stepDelayMs);
    }
  }

  public void markFailure(UUID missionId) {
    transactionTemplate.executeWithoutResult(
        status ->
            missionRepository
                .findById(missionId)
                .ifPresent(
                    m -> {
                      if (m.getStatus() == MissionStatus.FALHA_PROCESSAMENTO) {
                        return;
                      }
                      MissionStatus prev = m.getStatus();
                      m.setStatus(MissionStatus.FALHA_PROCESSAMENTO);
                      m.setUltimaAtualizacao(Instant.now());
                      missionRepository.save(m);
                      missionHistoryRecorder.record(
                          m,
                          prev,
                          MissionStatus.FALHA_PROCESSAMENTO,
                          "Falha ou interrupção no processamento assíncrono.",
                          MissionHistoryOrigin.KAFKA_WORKFLOW_ERRO);
                    }));
    missionRealtimeNotifier.notifyMissionUpdate(missionId);
  }
}
