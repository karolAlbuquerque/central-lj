package br.edu.central.centrallj.repository;

import br.edu.central.centrallj.domain.EquipeHeroica;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeHeroicaRepository extends JpaRepository<EquipeHeroica, UUID> {}
