package br.edu.central.centrallj.adapter.in.web.dto;

import br.edu.central.centrallj.domain.SabotageScope;
import br.edu.central.centrallj.domain.SabotageType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SabotageChoiceRequest(
    @NotNull SabotageType sabotageType,
    @NotNull SabotageScope targetScope,
    UUID targetUserId) {}
