package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.HeroiEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HeroiRepository extends JpaRepository<HeroiEntity, UUID> {

  List<HeroiEntity> findByEquipe_IdOrderByNomeHeroicoAsc(UUID equipeId);

  @EntityGraph(attributePaths = {"equipe"})
  List<HeroiEntity> findAllByOrderByNomeHeroicoAsc();

  @Query("select h from HeroiEntity h left join fetch h.equipe where h.id = :id")
  Optional<HeroiEntity> findByIdWithEquipe(@Param("id") UUID id);
}
