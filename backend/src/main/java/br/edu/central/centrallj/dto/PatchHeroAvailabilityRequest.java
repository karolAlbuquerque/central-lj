package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import jakarta.validation.constraints.NotNull;

public record PatchHeroAvailabilityRequest(@NotNull HeroiDisponibilidade disponibilidade) {}
