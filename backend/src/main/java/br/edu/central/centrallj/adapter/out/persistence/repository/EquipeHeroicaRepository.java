package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.EquipeHeroicaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeHeroicaRepository extends JpaRepository<EquipeHeroicaEntity, UUID> {}
