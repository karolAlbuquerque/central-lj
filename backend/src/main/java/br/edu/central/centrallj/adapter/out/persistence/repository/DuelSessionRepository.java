package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.DuelSessionEntity;
import br.edu.central.centrallj.domain.DuelStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DuelSessionRepository extends JpaRepository<DuelSessionEntity, UUID> {
  Optional<DuelSessionEntity> findByMissionIdAndStatusIn(UUID missionId, List<DuelStatus> statuses);
  boolean existsByMissionIdAndStatusIn(UUID missionId, List<DuelStatus> statuses);
}
