package br.edu.central.centrallj.adapter.out.persistence.entity;

import br.edu.central.centrallj.domain.DuelStatus;
import br.edu.central.centrallj.domain.PuzzleType;
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
@Table(name = "duel_sessions")
public class DuelSessionEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private PlayerMissionEntity mission;

  @Column(name = "attacker_user_id", nullable = false)
  private UUID attackerUserId;

  @Column(name = "defender_user_id", nullable = false)
  private UUID defenderUserId;

  @Column(nullable = false, length = 64)
  private String seed;

  @Enumerated(EnumType.STRING)
  @Column(name = "puzzle_type", nullable = false, length = 50)
  private PuzzleType puzzleType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private DuelStatus status;

  @Column(name = "round_current", nullable = false)
  private int roundCurrent;

  @Column(name = "round_max", nullable = false)
  private int roundMax;

  @Column(name = "attacker_rounds_won", nullable = false)
  private int attackerRoundsWon;

  @Column(name = "defender_rounds_won", nullable = false)
  private int defenderRoundsWon;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "timeout_at", nullable = false)
  private Instant timeoutAt;

  @Column(name = "infiltration_progress", nullable = false)
  private int infiltrationProgress;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public PlayerMissionEntity getMission() { return mission; }
  public void setMission(PlayerMissionEntity mission) { this.mission = mission; }

  public UUID getAttackerUserId() { return attackerUserId; }
  public void setAttackerUserId(UUID attackerUserId) { this.attackerUserId = attackerUserId; }

  public UUID getDefenderUserId() { return defenderUserId; }
  public void setDefenderUserId(UUID defenderUserId) { this.defenderUserId = defenderUserId; }

  public String getSeed() { return seed; }
  public void setSeed(String seed) { this.seed = seed; }

  public PuzzleType getPuzzleType() { return puzzleType; }
  public void setPuzzleType(PuzzleType puzzleType) { this.puzzleType = puzzleType; }

  public DuelStatus getStatus() { return status; }
  public void setStatus(DuelStatus status) { this.status = status; }

  public int getRoundCurrent() { return roundCurrent; }
  public void setRoundCurrent(int roundCurrent) { this.roundCurrent = roundCurrent; }

  public int getRoundMax() { return roundMax; }
  public void setRoundMax(int roundMax) { this.roundMax = roundMax; }

  public int getAttackerRoundsWon() { return attackerRoundsWon; }
  public void setAttackerRoundsWon(int attackerRoundsWon) { this.attackerRoundsWon = attackerRoundsWon; }

  public int getDefenderRoundsWon() { return defenderRoundsWon; }
  public void setDefenderRoundsWon(int defenderRoundsWon) { this.defenderRoundsWon = defenderRoundsWon; }

  public Instant getStartedAt() { return startedAt; }
  public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

  public Instant getFinishedAt() { return finishedAt; }
  public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

  public Instant getTimeoutAt() { return timeoutAt; }
  public void setTimeoutAt(Instant timeoutAt) { this.timeoutAt = timeoutAt; }

  public int getInfiltrationProgress() { return infiltrationProgress; }
  public void setInfiltrationProgress(int infiltrationProgress) {
    this.infiltrationProgress = infiltrationProgress;
  }
}
