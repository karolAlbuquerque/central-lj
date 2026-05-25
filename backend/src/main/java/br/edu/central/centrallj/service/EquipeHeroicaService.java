package br.edu.central.centrallj.service;

import br.edu.central.centrallj.application.port.in.ManageEquipeUseCase;
import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.domain.EquipeHeroica;
import br.edu.central.centrallj.dto.CreateEquipeRequest;
import br.edu.central.centrallj.dto.EquipeDetailResponse;
import br.edu.central.centrallj.dto.EquipeMapper;
import br.edu.central.centrallj.dto.EquipeResponse;
import br.edu.central.centrallj.dto.HeroMapper;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipeHeroicaService implements ManageEquipeUseCase {

  private final EquipePersistencePort equipePersistencePort;
  private final HeroiPersistencePort heroiPersistencePort;
  private final EquipeMapper equipeMapper;
  private final HeroMapper heroMapper;

  public EquipeHeroicaService(
      EquipePersistencePort equipePersistencePort,
      HeroiPersistencePort heroiPersistencePort,
      EquipeMapper equipeMapper,
      HeroMapper heroMapper) {
    this.equipePersistencePort = equipePersistencePort;
    this.heroiPersistencePort = heroiPersistencePort;
    this.equipeMapper = equipeMapper;
    this.heroMapper = heroMapper;
  }

  @Override
  @Transactional
  public EquipeResponse create(CreateEquipeRequest request) {
    EquipeHeroica e = new EquipeHeroica();
    e.setId(UUID.randomUUID());
    e.setNome(request.nome().trim());
    e.setEspecialidadePrincipal(blankToNull(request.especialidadePrincipal()));
    e.setAtiva(request.ativa());
    return equipeMapper.toResponse(equipePersistencePort.save(e));
  }

  @Override
  @Transactional(readOnly = true)
  public List<EquipeResponse> listAll() {
    return equipePersistencePort.findAll().stream()
        .sorted(java.util.Comparator.comparing(EquipeHeroica::getNome, String.CASE_INSENSITIVE_ORDER))
        .map(equipeMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public EquipeDetailResponse getDetail(UUID id) {
    EquipeHeroica equipe =
        equipePersistencePort
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + id));
    var membros =
        heroiPersistencePort.findByEquipeIdOrderByNomeHeroicoAsc(id).stream()
            .map(heroMapper::toResponse)
            .toList();
    return new EquipeDetailResponse(equipeMapper.toResponse(equipe), membros);
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
