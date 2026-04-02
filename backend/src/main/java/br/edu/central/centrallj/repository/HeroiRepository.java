package br.edu.central.centrallj.repository;

import br.edu.central.centrallj.domain.Heroi;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HeroiRepository extends JpaRepository<Heroi, UUID> {

  List<Heroi> findByEquipe_IdOrderByNomeHeroicoAsc(UUID equipeId);

  @EntityGraph(attributePaths = {"equipe"})
  List<Heroi> findAllByOrderByNomeHeroicoAsc();

  @Query("select h from Heroi h left join fetch h.equipe where h.id = :id")
  Optional<Heroi> findByIdWithEquipe(@Param("id") UUID id);
}
