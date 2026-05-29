package br.edu.central.centrallj.adapter.out.persistence.entity;

import br.edu.central.centrallj.domain.PuzzleType;
import br.edu.central.centrallj.domain.TaskStatus;
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
@Table(name = "mission_tasks")
public class MissionTaskEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private PlayerMissionEntity mission;

  @Column(name = "assigned_to_user_id")
  private UUID assignedToUserId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private TaskStatus status;

  @Column(name = "is_critical", nullable = false)
  private boolean critical;

  @Enumerated(EnumType.STRING)
  @Column(name = "puzzle_type", length = 50)
  private PuzzleType puzzleType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "depends_on_task_id")
  private MissionTaskEntity dependsOnTask;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UUID getId() { return id; }
  public void setId(UUID id) { this.id = id; }

  public PlayerMissionEntity getMission() { return mission; }
  public void setMission(PlayerMissionEntity mission) { this.mission = mission; }

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

  public MissionTaskEntity getDependsOnTask() { return dependsOnTask; }
  public void setDependsOnTask(MissionTaskEntity dependsOnTask) { this.dependsOnTask = dependsOnTask; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
