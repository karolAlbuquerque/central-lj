package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.mapper.EquipeApplicationMapper;
import br.edu.central.centrallj.application.mapper.HeroApplicationMapper;
import br.edu.central.centrallj.application.model.CreateEquipeCommand;
import br.edu.central.centrallj.application.model.EquipeDetailView;
import br.edu.central.centrallj.application.model.EquipeView;
import br.edu.central.centrallj.application.port.in.ManageEquipeUseCase;
import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.domain.EquipeHeroica;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipeHeroicaService implements ManageEquipeUseCase {

  private final EquipePersistencePort equipePersistencePort;
  private final HeroiPersistencePort heroiPersistencePort;
  private final EquipeApplicationMapper equipeMapper;
  private final HeroApplicationMapper heroMapper;

  public EquipeHeroicaService(
      EquipePersistencePort equipePersistencePort,
      HeroiPersistencePort heroiPersistencePort,
      EquipeApplicationMapper equipeMapper,
      HeroApplicationMapper heroMapper) {
    this.equipePersistencePort = equipePersistencePort;
    this.heroiPersistencePort = heroiPersistencePort;
    this.equipeMapper = equipeMapper;
    this.heroMapper = heroMapper;
  }

  @Override
  @Transactional
  public EquipeView create(CreateEquipeCommand command) {
    EquipeHeroica e = new EquipeHeroica();
    e.setId(UUID.randomUUID());
    e.setNome(command.nome().trim());
    e.setEspecialidadePrincipal(blankToNull(command.especialidadePrincipal()));
    e.setAtiva(command.ativa());
    return equipeMapper.toView(equipePersistencePort.save(e));
  }

  @Override
  @Transactional(readOnly = true)
  public List<EquipeView> listAll() {
    return equipePersistencePort.findAll().stream()
        .sorted(Comparator.comparing(EquipeHeroica::getNome, String.CASE_INSENSITIVE_ORDER))
        .map(equipeMapper::toView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public EquipeDetailView getDetail(UUID id) {
    EquipeHeroica equipe =
        equipePersistencePort
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + id));
    var membros =
        heroiPersistencePort.findByEquipeIdOrderByNomeHeroicoAsc(id).stream()
            .map(heroMapper::toView)
            .toList();
    return new EquipeDetailView(equipeMapper.toView(equipe), membros);
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
