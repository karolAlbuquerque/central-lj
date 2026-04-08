package br.edu.central.centrallj.application.port.in.teams;

import br.edu.central.centrallj.dto.CreateEquipeRequest;
import br.edu.central.centrallj.dto.EquipeDetailResponse;
import br.edu.central.centrallj.dto.EquipeResponse;
import java.util.List;
import java.util.UUID;

/** Porta de entrada: operações de equipe. */
public interface TeamUseCase {
  EquipeResponse create(CreateEquipeRequest request);

  List<EquipeResponse> listAll();

  EquipeDetailResponse getDetail(UUID id);
}

