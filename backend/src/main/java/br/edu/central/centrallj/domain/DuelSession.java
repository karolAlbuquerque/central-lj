package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class DuelSession {

  private UUID id;
  private UUID missionId;
  private UUID attackerUserId;
  private UUID defenderUserId;
  private String seed;
  private PuzzleType puzzleType;
  private DuelStatus status;
  private int roundCurrent;
  private int roundMax;
  private int attackerRoundsWon;
  private int defenderRoundsWon;
  private Instant startedAt;
  private Instant finishedAt;
  private Instant timeoutAt;
  /** Puzzles de infiltração concluídos (0–3) antes do duelo com o defensor. */
  private int infiltrationProgress;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getMissionId() { return missionId; }
  public void setMissionId(UUID missionId) { this.missionId = missionId; }

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
