package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class MissionTask {

  private UUID id;
  private UUID missionId;
  private UUID assignedToUserId;
  private String title;
  private String description;
  private TaskStatus status;
  private boolean critical;
  private PuzzleType puzzleType;
  private UUID dependsOnTaskId;
  private Instant createdAt;
  private Instant updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public UUID getMissionId() { return missionId; }
  public void setMissionId(UUID missionId) { this.missionId = missionId; }

  public UUID getAssignedToUserId() { return assignedToUserId; }
  public void setAssignedToUserId(UUID assignedToUserId) { this.assignedToUserId = assignedToUserId; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public TaskStatus getStatus() { return status; }
  public void setStatus(TaskStatus status) { this.status = status; }

  public boolean isCritical() { return critical; }
  public void setCritical(boolean critical) { this.critical = critical; }

  public PuzzleType getPuzzleType() { return puzzleType; }
  public void setPuzzleType(PuzzleType puzzleType) { this.puzzleType = puzzleType; }

  public UUID getDependsOnTaskId() { return dependsOnTaskId; }
  public void setDependsOnTaskId(UUID dependsOnTaskId) { this.dependsOnTaskId = dependsOnTaskId; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
