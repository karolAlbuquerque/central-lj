package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.AssignTaskCommand;
import br.edu.central.centrallj.application.model.MissionTaskView;

public interface AssignMissionTaskUseCase {
  MissionTaskView createTask(AssignTaskCommand command);
  MissionTaskView updateTask(AssignTaskCommand command);
}
