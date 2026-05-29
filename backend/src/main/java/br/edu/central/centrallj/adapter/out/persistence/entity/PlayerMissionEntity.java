package br.edu.central.centrallj.adapter.out.persistence.entity;

import br.edu.central.centrallj.domain.MissionCombatState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_missions")
public class PlayerMissionEntity {

  @Id private UUID id;

  @Column(nullable = false, length = 300)
  private String titulo;

  @Column(columnDefinition = "text")
  private String descricao;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "combat_state", nullable = false, length = 50)
  private MissionCombatState combatState;

  @Column(name = "min_players", nullable = false)
  private Integer minPlayers;

  @Column(name = "party_seed", length = 64)
  private String partySeed;

  @Column(name = "started_by_user_id")
  private UUID startedByUserId;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
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
