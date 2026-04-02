package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateHeroRequest(
    @NotBlank @Size(max = 200) String nomeHeroico,
    @Size(max = 200) String nomeCivil,
    @NotBlank @Size(max = 200) String especialidade,
    @NotNull HeroiDisponibilidade statusDisponibilidade,
    @NotBlank @Size(max = 50) String nivel,
    boolean ativo,
    UUID equipeId) {}
