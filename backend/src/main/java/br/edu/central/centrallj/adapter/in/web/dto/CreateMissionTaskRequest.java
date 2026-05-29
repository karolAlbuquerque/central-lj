package br.edu.central.centrallj.adapter.in.web.dto;

import br.edu.central.centrallj.domain.PuzzleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateMissionTaskRequest(
    @NotBlank @Size(max = 200) String title,
    String description,
    UUID assignedToUserId,
    boolean critical,
    PuzzleType puzzleType,
    UUID dependsOnTaskId) {}
