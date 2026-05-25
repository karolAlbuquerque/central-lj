package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.DashboardSummaryView;
import br.edu.central.centrallj.application.model.MissionDetailView;
import br.edu.central.centrallj.application.model.MissionHistoryEntryView;
import br.edu.central.centrallj.application.model.MissionView;
import br.edu.central.centrallj.domain.MissionStatus;
import java.util.List;
import java.util.UUID;

public interface GetMissionUseCase {
  List<MissionView> listAll();

  List<MissionView> listByStatus(MissionStatus status);

  List<MissionView> recentMissions();

  MissionDetailView getDetail(UUID id);

  List<MissionHistoryEntryView> getHistory(UUID id);

  DashboardSummaryView dashboardSummary();
}
