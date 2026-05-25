package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import java.time.Instant;
import java.util.UUID;

public record MissionView(
    UUID id,
    String titulo,
    String descricao,
    TipoAmeaca tipoAmeaca,
    PrioridadeMissao prioridade,
    MissionStatus status,
    Instant dataCriacao,
    Instant ultimaAtualizacao,
    MissionLocationView localizacao,
    MissionAssignmentView atribuicao) {}
