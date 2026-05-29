package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.MissionCombatState;
import br.edu.central.centrallj.domain.PlayerMission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerMissionPersistencePort {
  PlayerMission save(PlayerMission mission);
  Optional<PlayerMission> findById(UUID id);
  List<PlayerMission> findAll();
  List<PlayerMission> findByOwner(UUID ownerUserId);
  List<PlayerMission> findByStates(List<MissionCombatState> states);
}
