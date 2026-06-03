package br.edu.central.centrallj.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "puzzle_attempts")
public class PuzzleAttemptEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "duel_session_id", nullable = false)
  private DuelSessionEntity duelSession;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "round_number", nullable = false)
  private int roundNumber;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "move_sequence", columnDefinition = "jsonb")
  private String moveSequence;

  @Column(name = "is_valid")
  private Boolean valid;

  @Column(name = "time_ms")
  private Integer timeMs;

  @Column(name = "submitted_at", nullable = false)
  private Instant submittedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public DuelSessionEntity getDuelSession() { return duelSession; }
  public void setDuelSession(DuelSessionEntity duelSession) { this.duelSession = duelSession; }

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }

  public int getRoundNumber() { return roundNumber; }
  public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

  public String getMoveSequence() { return moveSequence; }
  public void setMoveSequence(String moveSequence) { this.moveSequence = moveSequence; }

  public Boolean getValid() { return valid; }
  public void setValid(Boolean valid) { this.valid = valid; }

  public Integer getTimeMs() { return timeMs; }
  public void setTimeMs(Integer timeMs) { this.timeMs = timeMs; }

  public Instant getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
