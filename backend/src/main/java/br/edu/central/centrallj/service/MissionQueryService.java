package br.edu.central.centrallj.service;

import br.edu.central.centrallj.application.port.in.GetMissionUseCase;
import br.edu.central.centrallj.application.port.out.MissionHistoryPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.dto.DashboardSummaryResponse;
import br.edu.central.centrallj.dto.MissionDetailResponse;
import br.edu.central.centrallj.dto.MissionHistoryEntryResponse;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
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
  private final MissionMapper missionMapper;

  public MissionQueryService(
      MissionPersistencePort missionPersistencePort,
      MissionHistoryPersistencePort missionHistoryPersistencePort,
      MissionMapper missionMapper) {
    this.missionPersistencePort = missionPersistencePort;
    this.missionHistoryPersistencePort = missionHistoryPersistencePort;
    this.missionMapper = missionMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionResponse> listAll() {
    return missionPersistencePort.findAllByOrderByDataCriacaoDesc().stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionHistoryEntryResponse> getHistory(UUID id) {
    if (!missionPersistencePort.existsById(id)) {
      throw new ResourceNotFoundException("Missão não encontrada: " + id);
    }
    return missionHistoryPersistencePort.findByMissionIdOrderByOcorridoEmAsc(id).stream()
        .map(missionMapper::toHistoryEntry)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public MissionDetailResponse getDetail(UUID id) {
    var mission =
        missionPersistencePort
            .findByIdWithAssignments(id)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + id));
    var historico =
        missionHistoryPersistencePort.findByMissionIdOrderByOcorridoEmAsc(id).stream()
            .map(missionMapper::toHistoryEntry)
            .toList();
    return new MissionDetailResponse(missionMapper.toResponse(mission), historico);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionResponse> listByStatus(MissionStatus status) {
    return missionPersistencePort.findByStatusOrderByDataCriacaoDesc(status).stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionResponse> recentMissions() {
    return missionPersistencePort.findRecent(PageRequest.of(0, 12)).stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardSummaryResponse dashboardSummary() {
    long total = missionPersistencePort.count();
    long emAndamento = missionPersistencePort.countByStatusIn(EM_ANDAMENTO_STATUS);
    long concluidas = missionPersistencePort.countByStatus(MissionStatus.CONCLUIDA);
    long falhas = missionPersistencePort.countByStatus(MissionStatus.FALHA_PROCESSAMENTO);
    List<MissionResponse> recentes =
        missionPersistencePort.findRecent(PageRequest.of(0, 12)).stream()
            .map(missionMapper::toResponse)
            .toList();
    return new DashboardSummaryResponse(total, emAndamento, concluidas, falhas, recentes);
  }
}
