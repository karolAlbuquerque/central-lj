package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.ExecuteTaskCommand;
import br.edu.central.centrallj.application.model.MissionTaskView;

public interface ExecuteMissionTaskUseCase {
  MissionTaskView execute(ExecuteTaskCommand command);
}
