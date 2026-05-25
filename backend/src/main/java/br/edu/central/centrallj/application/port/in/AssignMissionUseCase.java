package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.dto.AssignHeroRequest;
import br.edu.central.centrallj.dto.AssignTeamRequest;
import br.edu.central.centrallj.dto.MissionResponse;
import java.util.UUID;

public interface AssignMissionUseCase {
  MissionResponse assignHero(UUID missionId, AssignHeroRequest request);
  MissionResponse assignTeam(UUID missionId, AssignTeamRequest request);
}
