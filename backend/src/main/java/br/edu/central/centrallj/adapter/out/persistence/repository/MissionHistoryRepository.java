package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.MissionHistoryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionHistoryRepository extends JpaRepository<MissionHistoryEntity, UUID> {

  List<MissionHistoryEntity> findByMission_IdOrderByOcorridoEmAsc(UUID missionId);
}
