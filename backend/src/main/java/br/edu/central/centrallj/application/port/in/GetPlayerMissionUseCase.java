package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.PlayerMissionDetailView;
import br.edu.central.centrallj.application.model.PlayerMissionView;
import java.util.List;
import java.util.UUID;

public interface GetPlayerMissionUseCase {
  PlayerMissionDetailView getDetail(UUID missionId, UUID requestingUserId);
  List<PlayerMissionView> listByOwner(UUID ownerUserId);
  List<PlayerMissionView> listForMember(UUID userId);
  List<PlayerMissionView> listAll();
}
