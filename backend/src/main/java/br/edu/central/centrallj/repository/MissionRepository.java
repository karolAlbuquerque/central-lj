package br.edu.central.centrallj.repository;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, UUID> {

  List<Mission> findByStatusOrderByDataCriacaoDesc(MissionStatus status);

  List<Mission> findAllByOrderByDataCriacaoDesc();

  long countByStatus(MissionStatus status);

  long countByStatusIn(Collection<MissionStatus> statuses);

  List<Mission> findTop12ByOrderByDataCriacaoDesc();
}
