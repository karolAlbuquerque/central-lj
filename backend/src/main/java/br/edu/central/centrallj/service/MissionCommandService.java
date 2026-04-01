package br.edu.central.centrallj.service;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.dto.CreateMissionRequest;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.messaging.event.MissionCreatedEventFactory;
import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.messaging.producer.MissionCreatedEventProducer;
import br.edu.central.centrallj.repository.MissionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class MissionCommandService {

  private final MissionRepository missionRepository;
  private final MissionMapper missionMapper;
  private final MissionCreatedEventFactory eventFactory;
  private final MissionCreatedEventProducer createdEventProducer;
  private final MissionHistoryRecorder missionHistoryRecorder;
  private final MissionRealtimeNotifier missionRealtimeNotifier;

  public MissionCommandService(
      MissionRepository missionRepository,
      MissionMapper missionMapper,
      MissionCreatedEventFactory eventFactory,
      MissionCreatedEventProducer createdEventProducer,
      MissionHistoryRecorder missionHistoryRecorder,
      MissionRealtimeNotifier missionRealtimeNotifier) {
    this.missionRepository = missionRepository;
    this.missionMapper = missionMapper;
    this.eventFactory = eventFactory;
    this.createdEventProducer = createdEventProducer;
    this.missionHistoryRecorder = missionHistoryRecorder;
    this.missionRealtimeNotifier = missionRealtimeNotifier;
  }

  @Transactional
  public MissionResponse create(CreateMissionRequest request) {
    Instant now = Instant.now();
    Mission m = new Mission();
    m.setId(UUID.randomUUID());
    m.setTitulo(request.titulo().trim());
    m.setDescricao(blankToNull(request.descricao()));
    m.setTipoAmeaca(request.tipoAmeaca());
    m.setPrioridade(request.prioridade());
    m.setStatus(MissionStatus.RECEBIDA);
    m.setDataCriacao(now);
    m.setUltimaAtualizacao(now);
    m.setCidade(blankToNull(request.cidade()));
    m.setBairro(blankToNull(request.bairro()));
    m.setReferencia(blankToNull(request.referencia()));

    Mission saved = missionRepository.save(m);
    missionHistoryRecorder.record(
        saved,
        null,
        MissionStatus.RECEBIDA,
        "Missão recebida na central; aguardando processamento assíncrono (Kafka).",
        MissionHistoryOrigin.API_REGISTRO);

    MissionCreatedKafkaEvent event = eventFactory.created(saved);
    UUID missionId = saved.getId();
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            createdEventProducer.publish(event);
            missionRealtimeNotifier.notifyMissionUpdate(missionId);
          }
        });

    return missionMapper.toResponse(saved);
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
