package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.MissionTaskEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionTaskRepository extends JpaRepository<MissionTaskEntity, UUID> {
  List<MissionTaskEntity> findByMissionId(UUID missionId);
}
