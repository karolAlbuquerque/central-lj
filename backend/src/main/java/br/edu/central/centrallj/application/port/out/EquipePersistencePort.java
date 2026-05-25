package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.EquipeHeroica;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipePersistencePort {
  EquipeHeroica save(EquipeHeroica equipe);
  Optional<EquipeHeroica> findById(UUID id);
  List<EquipeHeroica> findAll();
}
