package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.mapper.PersistenceEntityMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.EquipeHeroicaRepository;
import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.domain.EquipeHeroica;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EquipeJpaPersistenceAdapter implements EquipePersistencePort {

  private final EquipeHeroicaRepository jpa;
  private final PersistenceEntityMapper mapper;

  public EquipeJpaPersistenceAdapter(EquipeHeroicaRepository jpa, PersistenceEntityMapper mapper) {
    this.jpa = jpa;
    this.mapper = mapper;
  }

  @Override
  public EquipeHeroica save(EquipeHeroica equipe) {
    return mapper.toDomain(jpa.save(mapper.toEntity(equipe)));
  }

  @Override
  public Optional<EquipeHeroica> findById(UUID id) {
    return jpa.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<EquipeHeroica> findAll() {
    return jpa.findAll().stream().map(mapper::toDomain).toList();
  }
}
