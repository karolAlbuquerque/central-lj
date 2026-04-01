package br.edu.central.centrallj.service.workflow;

import br.edu.central.centrallj.domain.MissionStatus;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultMissionProcessingFlowStrategy implements MissionProcessingFlowStrategy {

  @Override
  public List<MissionStatus> orderedStepsAfterRecebida() {
    return List.of(
        MissionStatus.EM_ANALISE,
        MissionStatus.PRIORIZADA,
        MissionStatus.EQUIPE_DESIGNADA,
        MissionStatus.EM_ANDAMENTO,
        MissionStatus.CONCLUIDA);
  }
}
