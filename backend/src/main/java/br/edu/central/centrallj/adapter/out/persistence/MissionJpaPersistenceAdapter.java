package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.repository.MissionRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class MissionJpaPersistenceAdapter implements MissionPersistencePort {

  private final MissionRepository jpa;

  public MissionJpaPersistenceAdapter(MissionRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Mission save(Mission mission) {
    return jpa.save(mission);
  }

  @Override
  public Optional<Mission> findById(UUID id) {
    return jpa.findById(id);
  }

  @Override
  public Optional<Mission> findByIdWithAssignments(UUID id) {
    return jpa.findByIdWithAssignments(id);
  }

  @Override
  public List<Mission> findAllByOrderByDataCriacaoDesc() {
    return jpa.findAllByOrderByDataCriacaoDesc();
  }

  @Override
  public List<Mission> findByStatusOrderByDataCriacaoDesc(MissionStatus status) {
    return jpa.findByStatusOrderByDataCriacaoDesc(status);
  }

  @Override
  public List<Mission> findRecent(Pageable pageable) {
    return jpa.findRecent(pageable);
  }

  @Override
  public List<Mission> findByHeroiResponsavelIdOrderByDataCriacaoDesc(UUID heroiId) {
    return jpa.findByHeroiResponsavel_IdOrderByDataCriacaoDesc(heroiId);
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
