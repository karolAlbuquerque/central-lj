package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class PuzzleAttempt {

  private UUID id;
  private UUID duelSessionId;
  private UUID userId;
  private int roundNumber;
  private String moveSequenceJson;
  private Boolean valid;
  private Integer timeMs;
  private Instant submittedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getDuelSessionId() { return duelSessionId; }
  public void setDuelSessionId(UUID duelSessionId) { this.duelSessionId = duelSessionId; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }

  public int getRoundNumber() { return roundNumber; }
  public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

  public String getMoveSequenceJson() { return moveSequenceJson; }
  public void setMoveSequenceJson(String moveSequenceJson) { this.moveSequenceJson = moveSequenceJson; }

  public Boolean getValid() { return valid; }
  public void setValid(Boolean valid) { this.valid = valid; }

  public Integer getTimeMs() { return timeMs; }
  public void setTimeMs(Integer timeMs) { this.timeMs = timeMs; }

  public Instant getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
