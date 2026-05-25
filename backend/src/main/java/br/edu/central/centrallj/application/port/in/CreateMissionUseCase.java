package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.dto.CreateMissionRequest;
import br.edu.central.centrallj.dto.MissionResponse;

public interface CreateMissionUseCase {
  MissionResponse create(CreateMissionRequest request);
}
