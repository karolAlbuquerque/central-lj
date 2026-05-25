package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.dto.DashboardSummaryResponse;
import br.edu.central.centrallj.dto.MissionDetailResponse;
import br.edu.central.centrallj.dto.MissionHistoryEntryResponse;
import br.edu.central.centrallj.dto.MissionResponse;
import java.util.List;
import java.util.UUID;

public interface GetMissionUseCase {
  List<MissionResponse> listAll();
  List<MissionResponse> listByStatus(MissionStatus status);
  List<MissionResponse> recentMissions();
  MissionDetailResponse getDetail(UUID id);
  List<MissionHistoryEntryResponse> getHistory(UUID id);
  DashboardSummaryResponse dashboardSummary();
}
