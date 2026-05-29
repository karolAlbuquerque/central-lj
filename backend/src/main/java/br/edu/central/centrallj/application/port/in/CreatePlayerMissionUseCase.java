package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.CreatePlayerMissionCommand;
import br.edu.central.centrallj.application.model.PlayerMissionView;

public interface CreatePlayerMissionUseCase {
  PlayerMissionView create(CreatePlayerMissionCommand command);
}
