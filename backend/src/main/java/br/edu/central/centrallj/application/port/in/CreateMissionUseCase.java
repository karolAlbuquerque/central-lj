package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.CreateMissionCommand;
import br.edu.central.centrallj.application.model.MissionView;

public interface CreateMissionUseCase {
  MissionView create(CreateMissionCommand command);
}
