package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.MissionMemberEntity;
import br.edu.central.centrallj.domain.MissionInviteStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionMemberRepository extends JpaRepository<MissionMemberEntity, UUID> {
  List<MissionMemberEntity> findByMissionId(UUID missionId);
  List<MissionMemberEntity> findByUserId(UUID userId);
  List<MissionMemberEntity> findByMissionIdAndInviteStatus(UUID missionId, MissionInviteStatus inviteStatus);
  Optional<MissionMemberEntity> findByMissionIdAndUserId(UUID missionId, UUID userId);
  long countByMissionIdAndInviteStatus(UUID missionId, MissionInviteStatus inviteStatus);
  boolean existsByMissionIdAndUserId(UUID missionId, UUID userId);
}
