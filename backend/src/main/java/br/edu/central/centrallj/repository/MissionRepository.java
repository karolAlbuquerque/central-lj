package br.edu.central.centrallj.repository;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MissionRepository extends JpaRepository<Mission, UUID> {

  @EntityGraph(attributePaths = {"heroiResponsavel", "equipeResponsavel"})
  @Query("select m from Mission m where m.status = :status order by m.dataCriacao desc")
  List<Mission> findByStatusOrderByDataCriacaoDesc(@Param("status") MissionStatus status);

  @EntityGraph(attributePaths = {"heroiResponsavel", "equipeResponsavel"})
  @Query("select m from Mission m order by m.dataCriacao desc")
  List<Mission> findAllByOrderByDataCriacaoDesc();

  long countByStatus(MissionStatus status);

  long countByStatusIn(Collection<MissionStatus> statuses);

  @EntityGraph(attributePaths = {"heroiResponsavel", "equipeResponsavel"})
  @Query("select m from Mission m order by m.dataCriacao desc")
  List<Mission> findRecent(Pageable pageable);

  @Query(
      "select m from Mission m left join fetch m.heroiResponsavel left join fetch m.equipeResponsavel where m.id = :id")
  Optional<Mission> findByIdWithAssignments(@Param("id") UUID id);

  @EntityGraph(attributePaths = {"heroiResponsavel", "equipeResponsavel"})
  List<Mission> findByHeroiResponsavel_IdOrderByDataCriacaoDesc(UUID heroiId);
}
