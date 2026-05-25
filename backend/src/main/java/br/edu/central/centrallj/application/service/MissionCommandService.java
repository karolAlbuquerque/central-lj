package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.application.event.MissionCreatedEvent;
import br.edu.central.centrallj.application.mapper.MissionApplicationMapper;
import br.edu.central.centrallj.application.model.CreateMissionCommand;
import br.edu.central.centrallj.application.model.MissionView;
import br.edu.central.centrallj.application.port.in.CreateMissionUseCase;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.application.support.AfterCommitMissionDispatch;
import br.edu.central.centrallj.application.support.MissionCreatedEventFactory;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.domain.MissionStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionCommandService implements CreateMissionUseCase {

  private final MissionPersistencePort missionPersistencePort;
  private final MissionApplicationMapper missionMapper;
  private final MissionCreatedEventFactory eventFactory;
  private final AfterCommitMissionDispatch afterCommitMissionDispatch;
  private final MissionHistoryRecorder missionHistoryRecorder;

  public MissionCommandService(
      MissionPersistencePort missionPersistencePort,
      MissionApplicationMapper missionMapper,
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
  public MissionView create(CreateMissionCommand command) {
    Instant now = Instant.now();
    Mission m = new Mission();
    m.setId(UUID.randomUUID());
    m.setTitulo(command.titulo().trim());
    m.setDescricao(blankToNull(command.descricao()));
    m.setTipoAmeaca(command.tipoAmeaca());
    m.setPrioridade(command.prioridade());
    m.setStatus(MissionStatus.RECEBIDA);
    m.setDataCriacao(now);
    m.setUltimaAtualizacao(now);
    m.setCidade(blankToNull(command.cidade()));
    m.setBairro(blankToNull(command.bairro()));
    m.setReferencia(blankToNull(command.referencia()));

    Mission saved = missionPersistencePort.save(m);
    missionHistoryRecorder.record(
        saved,
        null,
        MissionStatus.RECEBIDA,
        "Missão recebida na central; aguardando processamento assíncrono (Kafka).",
        MissionHistoryOrigin.API_REGISTRO);

    MissionCreatedEvent event = eventFactory.created(saved);
    afterCommitMissionDispatch.publishCreatedAndNotifyClients(event, saved.getId());

    return missionMapper.toView(saved);
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
