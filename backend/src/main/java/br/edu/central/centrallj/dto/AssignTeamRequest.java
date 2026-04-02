package br.edu.central.centrallj.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTeamRequest(@NotNull UUID equipeId, String atribuidoPor) {}
