package br.edu.central.centrallj.repository;

import br.edu.central.centrallj.domain.MissionHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionHistoryRepository extends JpaRepository<MissionHistory, UUID> {

  List<MissionHistory> findByMission_IdOrderByOcorridoEmAsc(UUID missionId);
}
