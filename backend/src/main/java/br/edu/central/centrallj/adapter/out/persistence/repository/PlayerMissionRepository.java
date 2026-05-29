package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.PlayerMissionEntity;
import br.edu.central.centrallj.domain.MissionCombatState;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerMissionRepository extends JpaRepository<PlayerMissionEntity, UUID> {
  List<PlayerMissionEntity> findByCombatStateIn(List<MissionCombatState> states);
  List<PlayerMissionEntity> findByOwnerUserId(UUID ownerUserId);
}
