package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.MissionMemberView;
import br.edu.central.centrallj.application.model.PlayerMissionView;
import java.util.UUID;

public interface InviteMissionMemberUseCase {
  MissionMemberView invite(UUID missionId, UUID invitedUserId, UUID requestingUserId);
  MissionMemberView acceptInvite(UUID missionId, UUID memberUserId);
  MissionMemberView declineInvite(UUID missionId, UUID memberUserId);
  PlayerMissionView startMission(UUID missionId, UUID requestingUserId, boolean forceStart);
}
