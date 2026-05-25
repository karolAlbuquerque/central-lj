package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.mapper.PersistenceEntityMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.HeroiRepository;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.domain.Heroi;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class HeroiJpaPersistenceAdapter implements HeroiPersistencePort {

  private final HeroiRepository jpa;
  private final PersistenceEntityMapper mapper;

  public HeroiJpaPersistenceAdapter(HeroiRepository jpa, PersistenceEntityMapper mapper) {
    this.jpa = jpa;
    this.mapper = mapper;
  }

  @Override
  public Heroi save(Heroi heroi) {
    return mapper.toDomain(jpa.save(mapper.toEntity(heroi)));
  }

  @Override
  public Optional<Heroi> findById(UUID id) {
    return jpa.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Heroi> findByIdWithEquipe(UUID id) {
    return jpa.findByIdWithEquipe(id).map(mapper::toDomain);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpa.existsById(id);
  }

  @Override
  public List<Heroi> findAllByOrderByNomeHeroicoAsc() {
    return jpa.findAllByOrderByNomeHeroicoAsc().stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Heroi> findByEquipeIdOrderByNomeHeroicoAsc(UUID equipeId) {
    return jpa.findByEquipe_IdOrderByNomeHeroicoAsc(equipeId).stream().map(mapper::toDomain).toList();
  }
}
