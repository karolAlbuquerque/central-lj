package br.edu.central.centrallj.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePlayerMissionRequest(
    @NotBlank @Size(max = 300) String titulo,
    String descricao,
    @Positive Integer minPlayers) {}
