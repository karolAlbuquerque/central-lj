package br.edu.central.centrallj.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTeamRequest(@NotNull UUID equipeId, String atribuidoPor) {}
