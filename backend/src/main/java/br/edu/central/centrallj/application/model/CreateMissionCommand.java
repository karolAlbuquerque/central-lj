package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;

public record CreateMissionCommand(
    String titulo,
    String descricao,
    TipoAmeaca tipoAmeaca,
    PrioridadeMissao prioridade,
    String cidade,
    String bairro,
    String referencia) {}
