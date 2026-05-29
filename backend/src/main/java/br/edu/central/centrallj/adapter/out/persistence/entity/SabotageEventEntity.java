package br.edu.central.centrallj.adapter.out.persistence.entity;

import br.edu.central.centrallj.domain.SabotageScope;
import br.edu.central.centrallj.domain.SabotageType;
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
@Table(name = "sabotage_events")
public class SabotageEventEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "duel_session_id", nullable = false)
  private DuelSessionEntity duelSession;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private PlayerMissionEntity mission;

  @Column(name = "attacker_user_id", nullable = false)
  private UUID attackerUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "sabotage_type", nullable = false, length = 50)
  private SabotageType sabotageType;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_scope", nullable = false, length = 50)
  private SabotageScope targetScope;

  @Column(name = "target_user_id")
  private UUID targetUserId;

  @Column(name = "applied_at", nullable = false)
  private Instant appliedAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "reversed_at")
  private Instant reversedAt;

  @Column(name = "effect_payload", columnDefinition = "jsonb")
  private String effectPayload;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public DuelSessionEntity getDuelSession() { return duelSession; }
  public void setDuelSession(DuelSessionEntity duelSession) { this.duelSession = duelSession; }

  public PlayerMissionEntity getMission() { return mission; }
  public void setMission(PlayerMissionEntity mission) { this.mission = mission; }

  public UUID getAttackerUserId() { return attackerUserId; }
  public void setAttackerUserId(UUID attackerUserId) { this.attackerUserId = attackerUserId; }

  public SabotageType getSabotageType() { return sabotageType; }
  public void setSabotageType(SabotageType sabotageType) { this.sabotageType = sabotageType; }

  public SabotageScope getTargetScope() { return targetScope; }
  public void setTargetScope(SabotageScope targetScope) { this.targetScope = targetScope; }

  public UUID getTargetUserId() { return targetUserId; }
  public void setTargetUserId(UUID targetUserId) { this.targetUserId = targetUserId; }

  public Instant getAppliedAt() { return appliedAt; }
  public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }

  public Instant getExpiresAt() { return expiresAt; }
  public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

  public Instant getReversedAt() { return reversedAt; }
  public void setReversedAt(Instant reversedAt) { this.reversedAt = reversedAt; }

  public String getEffectPayload() { return effectPayload; }
  public void setEffectPayload(String effectPayload) { this.effectPayload = effectPayload; }
}
