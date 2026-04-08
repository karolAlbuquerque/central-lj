package br.edu.central.centrallj.service;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.application.port.in.missions.MissionQueryUseCase;
import br.edu.central.centrallj.dto.DashboardSummaryResponse;
import br.edu.central.centrallj.dto.MissionDetailResponse;
import br.edu.central.centrallj.dto.MissionHistoryEntryResponse;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import br.edu.central.centrallj.repository.MissionHistoryRepository;
import br.edu.central.centrallj.repository.MissionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionQueryService implements MissionQueryUseCase {

  private static final List<MissionStatus> EM_ANDAMENTO_STATUS =
      List.of(
          MissionStatus.EM_ANALISE,
          MissionStatus.PRIORIZADA,
          MissionStatus.EQUIPE_DESIGNADA,
          MissionStatus.EM_ANDAMENTO);

  private final MissionRepository missionRepository;
  private final MissionHistoryRepository missionHistoryRepository;
  private final MissionMapper missionMapper;

  public MissionQueryService(
      MissionRepository missionRepository,
      MissionHistoryRepository missionHistoryRepository,
      MissionMapper missionMapper) {
    this.missionRepository = missionRepository;
    this.missionHistoryRepository = missionHistoryRepository;
    this.missionMapper = missionMapper;
  }

  @Transactional(readOnly = true)
  public List<MissionResponse> listAll() {
    return missionRepository.findAllByOrderByDataCriacaoDesc().stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<MissionHistoryEntryResponse> getHistory(UUID id) {
    if (!missionRepository.existsById(id)) {
      throw new ResourceNotFoundException("Missão não encontrada: " + id);
    }
    return missionHistoryRepository.findByMission_IdOrderByOcorridoEmAsc(id).stream()
        .map(missionMapper::toHistoryEntry)
        .toList();
  }

  @Transactional(readOnly = true)
  public MissionDetailResponse getDetail(UUID id) {
    var mission =
        missionRepository
            .findByIdWithAssignments(id)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + id));
    var historico =
        missionHistoryRepository.findByMission_IdOrderByOcorridoEmAsc(id).stream()
            .map(missionMapper::toHistoryEntry)
            .toList();
    return new MissionDetailResponse(missionMapper.toResponse(mission), historico);
  }

  @Transactional(readOnly = true)
  public List<MissionResponse> listByStatus(MissionStatus status) {
    return missionRepository.findByStatusOrderByDataCriacaoDesc(status).stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<MissionResponse> recentMissions() {
    return missionRepository.findRecent(PageRequest.of(0, 12)).stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public DashboardSummaryResponse dashboardSummary() {
    long total = missionRepository.count();
    long emAndamento = missionRepository.countByStatusIn(EM_ANDAMENTO_STATUS);
    long concluidas = missionRepository.countByStatus(MissionStatus.CONCLUIDA);
    long falhas = missionRepository.countByStatus(MissionStatus.FALHA_PROCESSAMENTO);
    List<MissionResponse> recentes =
        missionRepository.findRecent(PageRequest.of(0, 12)).stream()
            .map(missionMapper::toResponse)
            .toList();
    return new DashboardSummaryResponse(total, emAndamento, concluidas, falhas, recentes);
  }
}
