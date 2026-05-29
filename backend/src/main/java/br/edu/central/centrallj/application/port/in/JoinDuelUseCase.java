package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.DuelSessionView;
import java.util.UUID;

public interface JoinDuelUseCase {
  DuelSessionView join(UUID duelId, UUID defenderUserId);
}
