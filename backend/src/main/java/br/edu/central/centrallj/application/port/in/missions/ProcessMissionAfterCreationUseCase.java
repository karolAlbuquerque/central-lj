package br.edu.central.centrallj.application.port.in.missions;

import java.util.UUID;

/** Porta de entrada: processar workflow após evento missions.created. */
public interface ProcessMissionAfterCreationUseCase {
  void processAfterCreation(UUID missionId);
}

