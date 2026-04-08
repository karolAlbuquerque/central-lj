package br.edu.central.centrallj.application.port.in.missions;

import br.edu.central.centrallj.dto.AssignHeroRequest;
import br.edu.central.centrallj.dto.AssignTeamRequest;
import br.edu.central.centrallj.dto.MissionResponse;
import java.util.UUID;

/** Porta de entrada: atribuir herói ou equipe à missão (coordenação). */
public interface AssignMissionUseCase {
  MissionResponse assignHero(UUID missionId, AssignHeroRequest request);

  MissionResponse assignTeam(UUID missionId, AssignTeamRequest request);
}

