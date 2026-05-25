package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionPersistencePort {
  Mission save(Mission mission);
  Optional<Mission> findById(UUID id);
  Optional<Mission> findByIdWithAssignments(UUID id);
  List<Mission> findAllByOrderByDataCriacaoDesc();
  List<Mission> findByStatusOrderByDataCriacaoDesc(MissionStatus status);
  List<Mission> findRecent(int limit);
  List<Mission> findByHeroiResponsavelIdOrderByDataCriacaoDesc(UUID heroiId);
  long count();
  boolean existsById(UUID id);
  long countByStatus(MissionStatus status);
  long countByStatusIn(Collection<MissionStatus> statuses);
}
