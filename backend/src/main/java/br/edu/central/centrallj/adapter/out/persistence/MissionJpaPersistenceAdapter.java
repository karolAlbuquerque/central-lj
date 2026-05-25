package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.mapper.PersistenceEntityMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.MissionRepository;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class MissionJpaPersistenceAdapter implements MissionPersistencePort {

  private final MissionRepository jpa;
  private final PersistenceEntityMapper mapper;

  public MissionJpaPersistenceAdapter(MissionRepository jpa, PersistenceEntityMapper mapper) {
    this.jpa = jpa;
    this.mapper = mapper;
  }

  @Override
  public Mission save(Mission mission) {
    return mapper.toDomain(jpa.save(mapper.toEntity(mission)));
  }

  @Override
  public Optional<Mission> findById(UUID id) {
    return jpa.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Mission> findByIdWithAssignments(UUID id) {
    return jpa.findByIdWithAssignments(id).map(mapper::toDomain);
  }

  @Override
  public List<Mission> findAllByOrderByDataCriacaoDesc() {
    return jpa.findAllByOrderByDataCriacaoDesc().stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Mission> findByStatusOrderByDataCriacaoDesc(MissionStatus status) {
    return jpa.findByStatusOrderByDataCriacaoDesc(status).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Mission> findRecent(int limit) {
    return jpa.findRecent(PageRequest.of(0, limit)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Mission> findByHeroiResponsavelIdOrderByDataCriacaoDesc(UUID heroiId) {
    return jpa.findByHeroiResponsavel_IdOrderByDataCriacaoDesc(heroiId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public long count() {
    return jpa.count();
  }

  @Override
  public boolean existsById(UUID id) {
    return jpa.existsById(id);
  }

  @Override
  public long countByStatus(MissionStatus status) {
    return jpa.countByStatus(status);
  }

  @Override
  public long countByStatusIn(Collection<MissionStatus> statuses) {
    return jpa.countByStatusIn(statuses);
  }
}
