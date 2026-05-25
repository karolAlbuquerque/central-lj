package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.repository.HeroiRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class HeroiJpaPersistenceAdapter implements HeroiPersistencePort {

  private final HeroiRepository jpa;

  public HeroiJpaPersistenceAdapter(HeroiRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Heroi save(Heroi heroi) {
    return jpa.save(heroi);
  }

  @Override
  public Optional<Heroi> findById(UUID id) {
    return jpa.findById(id);
  }

  @Override
  public Optional<Heroi> findByIdWithEquipe(UUID id) {
    return jpa.findByIdWithEquipe(id);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpa.existsById(id);
  }

  @Override
  public List<Heroi> findAllByOrderByNomeHeroicoAsc() {
    return jpa.findAllByOrderByNomeHeroicoAsc();
  }

  @Override
  public List<Heroi> findByEquipeIdOrderByNomeHeroicoAsc(UUID equipeId) {
    return jpa.findByEquipe_IdOrderByNomeHeroicoAsc(equipeId);
  }
}
