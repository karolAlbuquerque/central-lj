package br.edu.central.centrallj.adapter.out.persistence.entity;

import br.edu.central.centrallj.domain.MissionMemberRole;
import br.edu.central.centrallj.domain.MissionInviteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mission_members")
public class MissionMemberEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private PlayerMissionEntity mission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UsuarioEntity user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private MissionMemberRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "invite_status", nullable = false, length = 20)
  private MissionInviteStatus inviteStatus;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  @Column(name = "invited_by_user_id")
  private UUID invitedByUserId;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public PlayerMissionEntity getMission() { return mission; }
  public void setMission(PlayerMissionEntity mission) { this.mission = mission; }

  public UsuarioEntity getUser() { return user; }
  public void setUser(UsuarioEntity user) { this.user = user; }

  public MissionMemberRole getRole() { return role; }
  public void setRole(MissionMemberRole role) { this.role = role; }

  public MissionInviteStatus getInviteStatus() { return inviteStatus; }
  public void setInviteStatus(MissionInviteStatus inviteStatus) { this.inviteStatus = inviteStatus; }

  public Instant getJoinedAt() { return joinedAt; }
  public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

  public UUID getInvitedByUserId() { return invitedByUserId; }
  public void setInvitedByUserId(UUID invitedByUserId) { this.invitedByUserId = invitedByUserId; }
}
