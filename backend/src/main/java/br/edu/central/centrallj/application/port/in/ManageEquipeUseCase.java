package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.CreateEquipeCommand;
import br.edu.central.centrallj.application.model.EquipeDetailView;
import br.edu.central.centrallj.application.model.EquipeView;
import java.util.List;
import java.util.UUID;

public interface ManageEquipeUseCase {
  EquipeView create(CreateEquipeCommand command);

  List<EquipeView> listAll();

  EquipeDetailView getDetail(UUID id);
}
