package br.edu.central.centrallj.dto;

import jakarta.validation.constraints.NotBlank;

public record MissionTestRequest(
    @NotBlank String titulo,
    String descricao,
    String tipoAmeaca,
    String prioridade
) {}

