package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.mapper.PvpPersistenceMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.PlayerMissionRepository;
import br.edu.central.centrallj.application.port.out.PlayerMissionPersistencePort;
import br.edu.central.centrallj.domain.MissionCombatState;
import br.edu.central.centrallj.domain.PlayerMission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PlayerMissionJpaPersistenceAdapter implements PlayerMissionPersistencePort {

  private final PlayerMissionRepository repository;
  private final PvpPersistenceMapper mapper;

  public PlayerMissionJpaPersistenceAdapter(
      PlayerMissionRepository repository, PvpPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public PlayerMission save(PlayerMission mission) {
    return mapper.toDomain(repository.save(mapper.toEntity(mission)));
  }

  @Override
  public Optional<PlayerMission> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<PlayerMission> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<PlayerMission> findByOwner(UUID ownerUserId) {
    return repository.findByOwnerUserId(ownerUserId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<PlayerMission> findByStates(List<MissionCombatState> states) {
    return repository.findByCombatStateIn(states).stream().map(mapper::toDomain).toList();
  }
}
