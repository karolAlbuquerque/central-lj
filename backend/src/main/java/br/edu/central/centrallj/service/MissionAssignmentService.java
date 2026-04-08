package br.edu.central.centrallj.service;

import br.edu.central.centrallj.domain.EquipeHeroica;
import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.application.port.in.missions.AssignMissionUseCase;
import br.edu.central.centrallj.dto.AssignHeroRequest;
import br.edu.central.centrallj.dto.AssignTeamRequest;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.exception.BadRequestException;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import br.edu.central.centrallj.repository.HeroiRepository;
import br.edu.central.centrallj.repository.MissionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionAssignmentService implements AssignMissionUseCase {

  private static final String ATRIB_PADRAO = "Coordenação";

  private final MissionRepository missionRepository;
  private final HeroiRepository heroiRepository;
  private final EquipeHeroicaService equipeHeroicaService;
  private final MissionHistoryRecorder missionHistoryRecorder;
  private final MissionRealtimeNotifier missionRealtimeNotifier;
  private final MissionMapper missionMapper;

  public MissionAssignmentService(
      MissionRepository missionRepository,
      HeroiRepository heroiRepository,
      EquipeHeroicaService equipeHeroicaService,
      MissionHistoryRecorder missionHistoryRecorder,
      MissionRealtimeNotifier missionRealtimeNotifier,
      MissionMapper missionMapper) {
    this.missionRepository = missionRepository;
    this.heroiRepository = heroiRepository;
    this.equipeHeroicaService = equipeHeroicaService;
    this.missionHistoryRecorder = missionHistoryRecorder;
    this.missionRealtimeNotifier = missionRealtimeNotifier;
    this.missionMapper = missionMapper;
  }

  @Transactional
  public MissionResponse assignHero(UUID missionId, AssignHeroRequest request) {
    Mission mission =
        missionRepository
            .findByIdWithAssignments(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + missionId));
    Heroi hero =
        heroiRepository
            .findById(request.heroiId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Herói não encontrado: " + request.heroiId()));
    if (!hero.isAtivo()) {
      throw new BadRequestException("Herói inativo não pode ser designado.");
    }

    liberarHeroiAnterior(mission);
    mission.setEquipeResponsavel(null);
    mission.setHeroiResponsavel(hero);
    Instant agora = Instant.now();
    mission.setAtribuidoEm(agora);
    mission.setAtribuidoPor(atribuidoPor(request.atribuidoPor()));
    mission.setUltimaAtualizacao(agora);

    if (hero.getStatusDisponibilidade() == HeroiDisponibilidade.DISPONIVEL) {
      hero.setStatusDisponibilidade(HeroiDisponibilidade.EM_MISSAO);
    }

    missionRepository.save(mission);
    heroiRepository.save(hero);

    var st = mission.getStatus();
    missionHistoryRecorder.record(
        mission,
        st,
        st,
        "Herói \"" + hero.getNomeHeroico() + "\" designado à missão.",
        MissionHistoryOrigin.API_ATRIBUICAO);

    missionRealtimeNotifier.notifyMissionUpdate(missionId);
    return missionMapper.toResponse(recarregarMission(missionId));
  }

  @Transactional
  public MissionResponse assignTeam(UUID missionId, AssignTeamRequest request) {
    Mission mission =
        missionRepository
            .findByIdWithAssignments(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + missionId));
    EquipeHeroica equipe = equipeHeroicaService.requireById(request.equipeId());
    if (!equipe.isAtiva()) {
      throw new BadRequestException("Equipe inativa não pode ser designada.");
    }

    liberarHeroiAnterior(mission);
    mission.setHeroiResponsavel(null);
    mission.setEquipeResponsavel(equipe);
    Instant agora = Instant.now();
    mission.setAtribuidoEm(agora);
    mission.setAtribuidoPor(atribuidoPor(request.atribuidoPor()));
    mission.setUltimaAtualizacao(agora);
    missionRepository.save(mission);

    var st = mission.getStatus();
    missionHistoryRecorder.record(
        mission,
        st,
        st,
        "Equipe \"" + equipe.getNome() + "\" designada à missão.",
        MissionHistoryOrigin.API_ATRIBUICAO);

    missionRealtimeNotifier.notifyMissionUpdate(missionId);
    return missionMapper.toResponse(recarregarMission(missionId));
  }

  private Mission recarregarMission(UUID missionId) {
    return missionRepository
        .findByIdWithAssignments(missionId)
        .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + missionId));
  }

  private void liberarHeroiAnterior(Mission mission) {
    Heroi anterior = mission.getHeroiResponsavel();
    if (anterior != null
        && anterior.getStatusDisponibilidade() == HeroiDisponibilidade.EM_MISSAO) {
      anterior.setStatusDisponibilidade(HeroiDisponibilidade.DISPONIVEL);
      heroiRepository.save(anterior);
    }
  }

  private static String atribuidoPor(String s) {
    if (s == null || s.isBlank()) {
      return ATRIB_PADRAO;
    }
    return s.trim();
  }
}
