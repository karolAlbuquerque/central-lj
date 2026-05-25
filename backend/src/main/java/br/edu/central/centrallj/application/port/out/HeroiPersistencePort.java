package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.Heroi;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HeroiPersistencePort {
  Heroi save(Heroi heroi);
  Optional<Heroi> findById(UUID id);
  Optional<Heroi> findByIdWithEquipe(UUID id);
  boolean existsById(UUID id);
  List<Heroi> findAllByOrderByNomeHeroicoAsc();
  List<Heroi> findByEquipeIdOrderByNomeHeroicoAsc(UUID equipeId);
}
