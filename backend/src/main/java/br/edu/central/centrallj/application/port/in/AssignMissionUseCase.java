package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.AssignHeroCommand;
import br.edu.central.centrallj.application.model.AssignTeamCommand;
import br.edu.central.centrallj.application.model.MissionView;
import java.util.UUID;

public interface AssignMissionUseCase {
  MissionView assignHero(UUID missionId, AssignHeroCommand command);

  MissionView assignTeam(UUID missionId, AssignTeamCommand command);
}
