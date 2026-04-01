package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMissionRequest(
    @NotBlank @Size(max = 500) String titulo,
    @Size(max = 5000) String descricao,
    @NotNull TipoAmeaca tipoAmeaca,
    @NotNull PrioridadeMissao prioridade,
    @Size(max = 200) String cidade,
    @Size(max = 200) String bairro,
    @Size(max = 500) String referencia) {}
