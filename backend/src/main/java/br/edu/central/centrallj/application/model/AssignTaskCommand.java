package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.PuzzleType;
import java.util.UUID;

public record AssignTaskCommand(
    UUID taskId,
    UUID missionId,
    UUID assignedToUserId,
    String title,
    String description,
    boolean critical,
    PuzzleType puzzleType,
    UUID dependsOnTaskId,
    UUID requestingUserId) {}
