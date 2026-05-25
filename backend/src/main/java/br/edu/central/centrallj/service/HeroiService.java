package br.edu.central.centrallj.service;

import br.edu.central.centrallj.application.port.in.ManageHeroiUseCase;
import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.dto.CreateHeroRequest;
import br.edu.central.centrallj.dto.HeroDetailResponse;
import br.edu.central.centrallj.dto.HeroMapper;
import br.edu.central.centrallj.dto.HeroResponse;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.dto.PatchHeroAvailabilityRequest;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeroiService implements ManageHeroiUseCase {

  private final HeroiPersistencePort heroiPersistencePort;
  private final EquipePersistencePort equipePersistencePort;
  private final MissionPersistencePort missionPersistencePort;
  private final HeroMapper heroMapper;
  private final MissionMapper missionMapper;

  public HeroiService(
      HeroiPersistencePort heroiPersistencePort,
      EquipePersistencePort equipePersistencePort,
      MissionPersistencePort missionPersistencePort,
      HeroMapper heroMapper,
      MissionMapper missionMapper) {
    this.heroiPersistencePort = heroiPersistencePort;
    this.equipePersistencePort = equipePersistencePort;
    this.missionPersistencePort = missionPersistencePort;
    this.heroMapper = heroMapper;
    this.missionMapper = missionMapper;
  }

  @Override
  @Transactional
  public HeroResponse create(CreateHeroRequest request) {
    Heroi h = new Heroi();
    h.setId(UUID.randomUUID());
    h.setNomeHeroico(request.nomeHeroico().trim());
    h.setNomeCivil(blankToNull(request.nomeCivil()));
    h.setEspecialidade(request.especialidade().trim());
    h.setStatusDisponibilidade(request.statusDisponibilidade());
    h.setNivel(request.nivel().trim());
    h.setAtivo(request.ativo());
    if (request.equipeId() != null) {
      h.setEquipe(
          equipePersistencePort
              .findById(request.equipeId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Equipe não encontrada: " + request.equipeId())));
    }
    return heroMapper.toResponse(heroiPersistencePort.save(h));
  }

  @Override
  @Transactional(readOnly = true)
  public List<HeroResponse> listAll() {
    return heroiPersistencePort.findAllByOrderByNomeHeroicoAsc().stream()
        .map(heroMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MissionResponse> listMissionsForHero(UUID heroiId) {
    if (!heroiPersistencePort.existsById(heroiId)) {
      throw new ResourceNotFoundException("Herói não encontrado: " + heroiId);
    }
    return missionPersistencePort.findByHeroiResponsavelIdOrderByDataCriacaoDesc(heroiId).stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public HeroDetailResponse getDetail(UUID id) {
    Heroi h =
        heroiPersistencePort
            .findByIdWithEquipe(id)
            .orElseThrow(() -> new ResourceNotFoundException("Herói não encontrado: " + id));
    var missoes =
        missionPersistencePort.findByHeroiResponsavelIdOrderByDataCriacaoDesc(id).stream()
            .map(missionMapper::toResponse)
            .toList();
    return new HeroDetailResponse(heroMapper.toResponse(h), missoes);
  }

  @Override
  @Transactional
  public HeroResponse patchAvailability(UUID id, PatchHeroAvailabilityRequest request) {
    Heroi h =
        heroiPersistencePort
            .findByIdWithEquipe(id)
            .orElseThrow(() -> new ResourceNotFoundException("Herói não encontrado: " + id));
    h.setStatusDisponibilidade(request.disponibilidade());
    return heroMapper.toResponse(heroiPersistencePort.save(h));
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
