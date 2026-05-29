package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class MissionMember {

  private UUID id;
  private UUID missionId;
  private UUID userId;
  private String userName;
  private MissionMemberRole role;
  private MissionInviteStatus inviteStatus;
  private Instant joinedAt;
  private UUID invitedByUserId;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getMissionId() { return missionId; }
  public void setMissionId(UUID missionId) { this.missionId = missionId; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }

  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }

  public MissionMemberRole getRole() { return role; }
  public void setRole(MissionMemberRole role) { this.role = role; }

  public MissionInviteStatus getInviteStatus() { return inviteStatus; }
  public void setInviteStatus(MissionInviteStatus inviteStatus) { this.inviteStatus = inviteStatus; }

  public Instant getJoinedAt() { return joinedAt; }
  public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

  public UUID getInvitedByUserId() { return invitedByUserId; }
  public void setInvitedByUserId(UUID invitedByUserId) { this.invitedByUserId = invitedByUserId; }
}
