package br.edu.central.centrallj.adapter.in.web.dto;

import br.edu.central.centrallj.domain.PuzzleType;
import br.edu.central.centrallj.domain.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record MissionTaskResponse(
    UUID id,
    UUID missionId,
    UUID assignedToUserId,
    String title,
    String description,
    TaskStatus status,
    boolean critical,
    UUID dependsOnTaskId,
    Instant createdAt,
    Instant updatedAt,
    boolean canExecute,
    String puzzleSeed,
    PuzzleType puzzleType) {}
