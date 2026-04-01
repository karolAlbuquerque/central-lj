package br.edu.central.centrallj.service.workflow;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import org.springframework.stereotype.Component;

@Component
public class MissionProcessingFlowStrategyResolver {

  private final DefaultMissionProcessingFlowStrategy defaultFlow;
  private final CriticalPriorityMissionProcessingFlowStrategy criticalFlow;

  public MissionProcessingFlowStrategyResolver(
      DefaultMissionProcessingFlowStrategy defaultFlow,
      CriticalPriorityMissionProcessingFlowStrategy criticalFlow) {
    this.defaultFlow = defaultFlow;
    this.criticalFlow = criticalFlow;
  }

  public MissionProcessingFlowStrategy resolve(Mission mission) {
    return mission.getPrioridade() == PrioridadeMissao.CRITICA ? criticalFlow : defaultFlow;
  }
}
