package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.PlayerMissionView;
import java.util.List;

public interface ListVillainTargetsUseCase {
  List<PlayerMissionView> listTargets();
}
