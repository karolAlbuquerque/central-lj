package br.edu.central.centrallj.service;

import br.edu.central.centrallj.application.port.in.CreateMissionUseCase;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.dto.CreateMissionRequest;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.messaging.event.MissionCreatedEventFactory;
import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import br.edu.central.centrallj.messaging.support.AfterCommitMissionDispatch;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionCommandService implements CreateMissionUseCase {

  private final MissionPersistencePort missionPersistencePort;
  private final MissionMapper missionMapper;
  private final MissionCreatedEventFactory eventFactory;
  private final AfterCommitMissionDispatch afterCommitMissionDispatch;
  private final MissionHistoryRecorder missionHistoryRecorder;

  public MissionCommandService(
      MissionPersistencePort missionPersistencePort,
      MissionMapper missionMapper,
      MissionCreatedEventFactory eventFactory,
      AfterCommitMissionDispatch afterCommitMissionDispatch,
      MissionHistoryRecorder missionHistoryRecorder) {
    this.missionPersistencePort = missionPersistencePort;
    this.missionMapper = missionMapper;
    this.eventFactory = eventFactory;
    this.afterCommitMissionDispatch = afterCommitMissionDispatch;
    this.missionHistoryRecorder = missionHistoryRecorder;
  }

  @Override
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

    Mission saved = missionPersistencePort.save(m);
    missionHistoryRecorder.record(
        saved,
        null,
        MissionStatus.RECEBIDA,
        "Missão recebida na central; aguardando processamento assíncrono (Kafka).",
        MissionHistoryOrigin.API_REGISTRO);

    MissionCreatedKafkaEvent event = eventFactory.created(saved);
    afterCommitMissionDispatch.publishCreatedAndNotifyClients(event, saved.getId());

    return missionMapper.toResponse(saved);
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
