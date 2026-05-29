package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.SabotageEventEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SabotageEventRepository extends JpaRepository<SabotageEventEntity, UUID> {
  List<SabotageEventEntity> findByMissionIdAndReversedAtIsNull(UUID missionId);
  List<SabotageEventEntity> findByReversedAtIsNullAndExpiresAtBefore(Instant now);
}
