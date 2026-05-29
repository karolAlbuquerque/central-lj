package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class PlayerMission {

  private UUID id;
  private String titulo;
  private String descricao;
  private UUID ownerUserId;
  private MissionCombatState combatState;
  private Integer minPlayers;
  private String partySeed;
  private UUID startedByUserId;
  private Instant startedAt;
  private Instant createdAt;
  private Instant updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public String getTitulo() { return titulo; }
  public void setTitulo(String titulo) { this.titulo = titulo; }

  public String getDescricao() { return descricao; }
  public void setDescricao(String descricao) { this.descricao = descricao; }

  public UUID getOwnerUserId() { return ownerUserId; }
  public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }

  public MissionCombatState getCombatState() { return combatState; }
  public void setCombatState(MissionCombatState combatState) { this.combatState = combatState; }

  public Integer getMinPlayers() { return minPlayers; }
  public void setMinPlayers(Integer minPlayers) { this.minPlayers = minPlayers; }

  public String getPartySeed() { return partySeed; }
  public void setPartySeed(String partySeed) { this.partySeed = partySeed; }

  public UUID getStartedByUserId() { return startedByUserId; }
  public void setStartedByUserId(UUID startedByUserId) { this.startedByUserId = startedByUserId; }

  public Instant getStartedAt() { return startedAt; }
  public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
