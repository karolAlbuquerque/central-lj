package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.DuelSessionView;
import br.edu.central.centrallj.application.model.SubmitProgressCommand;

public interface SubmitInfiltrationProgressUseCase {
  DuelSessionView submitInfiltrationProgress(SubmitProgressCommand command);
}
