package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.mapper.MissionApplicationMapper;
import br.edu.central.centrallj.application.model.DashboardSummaryView;
import br.edu.central.centrallj.application.model.MissionDetailView;
import br.edu.central.centrallj.application.model.MissionHistoryEntryView;
import br.edu.central.centrallj.application.model.MissionView;
import br.edu.central.centrallj.application.port.in.GetMissionUseCase;
import br.edu.central.centrallj.application.port.out.MissionHistoryPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.MissionStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionQueryService implements GetMissionUseCase {

  private static final List<MissionStatus> EM_ANDAMENTO_STATUS =
      List.of(
          MissionStatus.EM_ANALISE,
          MissionStatus.PRIORIZADA,
          MissionStatus.EQUIPE_DESIGNADA,
          MissionStatus.EM_ANDAMENTO);

  private final MissionPersistencePort missionPersistencePort;
  private final MissionHistoryPersistencePort missionHistoryPersistencePort;
  private final MissionApplicationMapper missionMapper;

  public MissionQueryService(
      MissionPersistencePort missionPersistencePort,
      MissionHistoryPersistencePort missionHistoryPersistencePort,
      MissionApplicationMapper missionMapper) {
    this.missionPersistencePort = missionPersistencePort;
    this.missionHistoryPersistencePort = missionHistoryPersistencePort;
    this.missionMapper = missionMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionView> listAll() {
    return missionPersistencePort.findAllByOrderByDataCriacaoDesc().stream()
        .map(missionMapper::toView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionHistoryEntryView> getHistory(UUID id) {
    if (!missionPersistencePort.existsById(id)) {
      throw new ResourceNotFoundException("Missão não encontrada: " + id);
    }
    return missionHistoryPersistencePort.findByMissionIdOrderByOcorridoEmAsc(id).stream()
        .map(missionMapper::toHistoryView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public MissionDetailView getDetail(UUID id) {
    var mission =
        missionPersistencePort
            .findByIdWithAssignments(id)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + id));
    var historico = missionHistoryPersistencePort.findByMissionIdOrderByOcorridoEmAsc(id);
    return missionMapper.toDetailView(mission, historico);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionView> listByStatus(MissionStatus status) {
    return missionPersistencePort.findByStatusOrderByDataCriacaoDesc(status).stream()
        .map(missionMapper::toView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionView> recentMissions() {
    return missionPersistencePort.findRecent(12).stream().map(missionMapper::toView).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardSummaryView dashboardSummary() {
    long total = missionPersistencePort.count();
    long emAndamento = missionPersistencePort.countByStatusIn(EM_ANDAMENTO_STATUS);
    long concluidas = missionPersistencePort.countByStatus(MissionStatus.CONCLUIDA);
    long falhas = missionPersistencePort.countByStatus(MissionStatus.FALHA_PROCESSAMENTO);
    List<MissionView> recentes =
        missionPersistencePort.findRecent(12).stream().map(missionMapper::toView).toList();
    return new DashboardSummaryView(total, emAndamento, concluidas, falhas, recentes);
  }
}
