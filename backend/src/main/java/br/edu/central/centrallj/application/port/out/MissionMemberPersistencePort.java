package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.MissionMember;
import br.edu.central.centrallj.domain.MissionInviteStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionMemberPersistencePort {
  MissionMember save(MissionMember member);
  List<MissionMember> findByMission(UUID missionId);
  List<MissionMember> findByMissionAndInviteStatus(UUID missionId, MissionInviteStatus inviteStatus);
  List<MissionMember> findByUser(UUID userId);
  long countByMissionAndInviteStatus(UUID missionId, MissionInviteStatus inviteStatus);
  Optional<MissionMember> findByMissionAndUser(UUID missionId, UUID userId);
  boolean existsByMissionAndUser(UUID missionId, UUID userId);
  void delete(UUID memberId);
}
