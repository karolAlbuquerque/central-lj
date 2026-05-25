package br.edu.central.centrallj.service;

import br.edu.central.centrallj.application.port.in.AssignMissionUseCase;
import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionNotificationPort;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.EquipeHeroica;
import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.dto.AssignHeroRequest;
import br.edu.central.centrallj.dto.AssignTeamRequest;
import br.edu.central.centrallj.dto.MissionMapper;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.exception.BadRequestException;
import br.edu.central.centrallj.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionAssignmentService implements AssignMissionUseCase {

  private static final String ATRIB_PADRAO = "Coordenação";

  private final MissionPersistencePort missionPersistencePort;
  private final HeroiPersistencePort heroiPersistencePort;
  private final EquipePersistencePort equipePersistencePort;
  private final MissionHistoryRecorder missionHistoryRecorder;
  private final MissionNotificationPort missionNotificationPort;
  private final MissionMapper missionMapper;

  public MissionAssignmentService(
      MissionPersistencePort missionPersistencePort,
      HeroiPersistencePort heroiPersistencePort,
      EquipePersistencePort equipePersistencePort,
      MissionHistoryRecorder missionHistoryRecorder,
      MissionNotificationPort missionNotificationPort,
      MissionMapper missionMapper) {
    this.missionPersistencePort = missionPersistencePort;
    this.heroiPersistencePort = heroiPersistencePort;
    this.equipePersistencePort = equipePersistencePort;
    this.missionHistoryRecorder = missionHistoryRecorder;
    this.missionNotificationPort = missionNotificationPort;
    this.missionMapper = missionMapper;
  }

  @Override
  @Transactional
  public MissionResponse assignHero(UUID missionId, AssignHeroRequest request) {
    Mission mission =
        missionPersistencePort
            .findByIdWithAssignments(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + missionId));
    Heroi hero =
        heroiPersistencePort
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

    missionPersistencePort.save(mission);
    heroiPersistencePort.save(hero);

    var st = mission.getStatus();
    missionHistoryRecorder.record(
        mission,
        st,
        st,
        "Herói \"" + hero.getNomeHeroico() + "\" designado à missão.",
        MissionHistoryOrigin.API_ATRIBUICAO);

    missionNotificationPort.notifyMissionUpdate(missionId);
    return missionMapper.toResponse(recarregarMission(missionId));
  }

  @Override
  @Transactional
  public MissionResponse assignTeam(UUID missionId, AssignTeamRequest request) {
    Mission mission =
        missionPersistencePort
            .findByIdWithAssignments(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + missionId));
    EquipeHeroica equipe =
        equipePersistencePort
            .findById(request.equipeId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Equipe não encontrada: " + request.equipeId()));
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
    missionPersistencePort.save(mission);

    var st = mission.getStatus();
    missionHistoryRecorder.record(
        mission,
        st,
        st,
        "Equipe \"" + equipe.getNome() + "\" designada à missão.",
        MissionHistoryOrigin.API_ATRIBUICAO);

    missionNotificationPort.notifyMissionUpdate(missionId);
    return missionMapper.toResponse(recarregarMission(missionId));
  }

  private Mission recarregarMission(UUID missionId) {
    return missionPersistencePort
        .findByIdWithAssignments(missionId)
        .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada: " + missionId));
  }

  private void liberarHeroiAnterior(Mission mission) {
    Heroi anterior = mission.getHeroiResponsavel();
    if (anterior != null
        && anterior.getStatusDisponibilidade() == HeroiDisponibilidade.EM_MISSAO) {
      anterior.setStatusDisponibilidade(HeroiDisponibilidade.DISPONIVEL);
      heroiPersistencePort.save(anterior);
    }
  }

  private static String atribuidoPor(String s) {
    if (s == null || s.isBlank()) {
      return ATRIB_PADRAO;
    }
    return s.trim();
  }
}
