package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class SabotageEvent {

  private UUID id;
  private UUID duelSessionId;
  private UUID missionId;
  private UUID attackerUserId;
  private SabotageType sabotageType;
  private SabotageScope targetScope;
  private UUID targetUserId;
  private Instant appliedAt;
  private Instant expiresAt;
  private Instant reversedAt;
  private String effectPayloadJson;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getDuelSessionId() { return duelSessionId; }
  public void setDuelSessionId(UUID duelSessionId) { this.duelSessionId = duelSessionId; }

  public UUID getMissionId() { return missionId; }
  public void setMissionId(UUID missionId) { this.missionId = missionId; }

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

  public String getEffectPayloadJson() { return effectPayloadJson; }
  public void setEffectPayloadJson(String effectPayloadJson) { this.effectPayloadJson = effectPayloadJson; }
}
