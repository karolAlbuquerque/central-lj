package br.edu.central.centrallj.service;

import br.edu.central.centrallj.domain.EquipeHeroica;
import br.edu.central.centrallj.dto.CreateEquipeRequest;
import br.edu.central.centrallj.dto.EquipeDetailResponse;
import br.edu.central.centrallj.dto.EquipeMapper;
import br.edu.central.centrallj.dto.EquipeResponse;
import br.edu.central.centrallj.dto.HeroMapper;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import br.edu.central.centrallj.repository.EquipeHeroicaRepository;
import br.edu.central.centrallj.repository.HeroiRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipeHeroicaService {

  private final EquipeHeroicaRepository equipeHeroicaRepository;
  private final HeroiRepository heroiRepository;
  private final EquipeMapper equipeMapper;
  private final HeroMapper heroMapper;

  public EquipeHeroicaService(
      EquipeHeroicaRepository equipeHeroicaRepository,
      HeroiRepository heroiRepository,
      EquipeMapper equipeMapper,
      HeroMapper heroMapper) {
    this.equipeHeroicaRepository = equipeHeroicaRepository;
    this.heroiRepository = heroiRepository;
    this.equipeMapper = equipeMapper;
    this.heroMapper = heroMapper;
  }

  @Transactional
  public EquipeResponse create(CreateEquipeRequest request) {
    EquipeHeroica e = new EquipeHeroica();
    e.setId(UUID.randomUUID());
    e.setNome(request.nome().trim());
    e.setEspecialidadePrincipal(blankToNull(request.especialidadePrincipal()));
    e.setAtiva(request.ativa());
    return equipeMapper.toResponse(equipeHeroicaRepository.save(e));
  }

  @Transactional(readOnly = true)
  public List<EquipeResponse> listAll() {
    return equipeHeroicaRepository.findAll().stream()
        .sorted(java.util.Comparator.comparing(EquipeHeroica::getNome, String.CASE_INSENSITIVE_ORDER))
        .map(equipeMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public EquipeDetailResponse getDetail(UUID id) {
    EquipeHeroica equipe =
        equipeHeroicaRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + id));
    var membros =
        heroiRepository.findByEquipe_IdOrderByNomeHeroicoAsc(id).stream()
            .map(heroMapper::toResponse)
            .toList();
    return new EquipeDetailResponse(equipeMapper.toResponse(equipe), membros);
  }

  public EquipeHeroica requireById(UUID id) {
    return equipeHeroicaRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + id));
  }

  private static String blankToNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }
}
