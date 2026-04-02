package br.edu.central.centrallj.service;

import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.dto.CreateHeroRequest;
import br.edu.central.centrallj.dto.HeroDetailResponse;
import br.edu.central.centrallj.dto.HeroMapper;
import br.edu.central.centrallj.dto.HeroResponse;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.PatchHeroAvailabilityRequest;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.repository.HeroiRepository;
import br.edu.central.centrallj.repository.MissionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeroiService {

  private final HeroiRepository heroiRepository;
  private final EquipeHeroicaService equipeHeroicaService;
  private final MissionRepository missionRepository;
  private final HeroMapper heroMapper;
  private final MissionMapper missionMapper;

  public HeroiService(
      HeroiRepository heroiRepository,
      EquipeHeroicaService equipeHeroicaService,
      MissionRepository missionRepository,
      HeroMapper heroMapper,
      MissionMapper missionMapper) {
    this.heroiRepository = heroiRepository;
    this.equipeHeroicaService = equipeHeroicaService;
    this.missionRepository = missionRepository;
    this.heroMapper = heroMapper;
    this.missionMapper = missionMapper;
  }

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
      h.setEquipe(equipeHeroicaService.requireById(request.equipeId()));
    }
    return heroMapper.toResponse(heroiRepository.save(h));
  }

  @Transactional(readOnly = true)
  public List<HeroResponse> listAll() {
    return heroiRepository.findAllByOrderByNomeHeroicoAsc().stream()
        .map(heroMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<MissionResponse> listMissionsForHero(UUID heroiId) {
    if (!heroiRepository.existsById(heroiId)) {
      throw new ResourceNotFoundException("Herói não encontrado: " + heroiId);
    }
    return missionRepository.findByHeroiResponsavel_IdOrderByDataCriacaoDesc(heroiId).stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public HeroDetailResponse getDetail(UUID id) {
    Heroi h =
        heroiRepository
            .findByIdWithEquipe(id)
            .orElseThrow(() -> new ResourceNotFoundException("Herói não encontrado: " + id));
    var missoes =
        missionRepository.findByHeroiResponsavel_IdOrderByDataCriacaoDesc(id).stream()
            .map(missionMapper::toResponse)
            .toList();
    return new HeroDetailResponse(heroMapper.toResponse(h), missoes);
  }

  @Transactional
  public HeroResponse patchAvailability(UUID id, PatchHeroAvailabilityRequest request) {
    Heroi h =
        heroiRepository
            .findByIdWithEquipe(id)
            .orElseThrow(() -> new ResourceNotFoundException("Herói não encontrado: " + id));
    h.setStatusDisponibilidade(request.disponibilidade());
    return heroMapper.toResponse(heroiRepository.save(h));
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
