package br.edu.central.centrallj.application.port.in;

import java.util.UUID;

public interface CloseMissionUseCase {
  void close(UUID missionId, UUID requestingUserId);
}
