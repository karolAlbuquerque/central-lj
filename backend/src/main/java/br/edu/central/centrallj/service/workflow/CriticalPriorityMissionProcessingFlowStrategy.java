package br.edu.central.centrallj.service.workflow;

import br.edu.central.centrallj.domain.MissionStatus;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Fluxo encurtado para prioridade CRÍTICA: pula análise inicial para simular despacho acelerado.
 */
@Component
public class CriticalPriorityMissionProcessingFlowStrategy implements MissionProcessingFlowStrategy {

  @Override
  public List<MissionStatus> orderedStepsAfterRecebida() {
    return List.of(
        MissionStatus.PRIORIZADA,
        MissionStatus.EQUIPE_DESIGNADA,
        MissionStatus.EM_ANDAMENTO,
        MissionStatus.CONCLUIDA);
  }
}
