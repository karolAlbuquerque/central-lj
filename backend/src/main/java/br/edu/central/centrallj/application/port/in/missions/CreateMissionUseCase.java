package br.edu.central.centrallj.application.port.in.missions;

import br.edu.central.centrallj.dto.CreateMissionRequest;
import br.edu.central.centrallj.dto.MissionResponse;

/** Porta de entrada: registrar uma missão (coordenação). */
public interface CreateMissionUseCase {
  MissionResponse create(CreateMissionRequest request);
}

