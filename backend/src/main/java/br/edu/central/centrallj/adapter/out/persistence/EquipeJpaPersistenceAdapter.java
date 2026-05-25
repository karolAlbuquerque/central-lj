package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.domain.EquipeHeroica;
import br.edu.central.centrallj.repository.EquipeHeroicaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EquipeJpaPersistenceAdapter implements EquipePersistencePort {

  private final EquipeHeroicaRepository jpa;

  public EquipeJpaPersistenceAdapter(EquipeHeroicaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public EquipeHeroica save(EquipeHeroica equipe) {
    return jpa.save(equipe);
  }

  @Override
  public Optional<EquipeHeroica> findById(UUID id) {
    return jpa.findById(id);
  }

  @Override
  public List<EquipeHeroica> findAll() {
    return jpa.findAll();
  }
}
