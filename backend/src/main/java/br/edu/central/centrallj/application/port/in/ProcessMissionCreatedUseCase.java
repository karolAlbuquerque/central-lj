package br.edu.central.centrallj.application.port.in;

import java.util.UUID;

/** Caso de uso acionado pelo adaptador Kafka após criação da missão. */
public interface ProcessMissionCreatedUseCase {
  void processAfterCreation(UUID missionId);
}
