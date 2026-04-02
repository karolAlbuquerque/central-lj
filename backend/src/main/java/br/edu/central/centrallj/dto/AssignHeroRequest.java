package br.edu.central.centrallj.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignHeroRequest(@NotNull UUID heroiId, String atribuidoPor) {}
