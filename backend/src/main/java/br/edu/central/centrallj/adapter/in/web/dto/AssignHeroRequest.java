package br.edu.central.centrallj.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignHeroRequest(@NotNull UUID heroiId, String atribuidoPor) {}
