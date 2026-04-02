package br.edu.central.centrallj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEquipeRequest(
    @NotBlank @Size(max = 200) String nome,
    @Size(max = 200) String especialidadePrincipal,
    boolean ativa) {}
