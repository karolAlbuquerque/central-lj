package br.edu.central.centrallj.service.workflow;

import br.edu.central.centrallj.domain.MissionStatus;
import java.util.List;

/**
 * Strategy: sequência de {@link MissionStatus} após a missão entrar como RECEBIDA, variando conforme
 * prioridade (ex.: fluxo crítico encurtado).
 */
public interface MissionProcessingFlowStrategy {

  List<MissionStatus> orderedStepsAfterRecebida();
}
