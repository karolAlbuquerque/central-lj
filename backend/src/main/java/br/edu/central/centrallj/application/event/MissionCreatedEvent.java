package br.edu.central.centrallj.application.event;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import java.time.Instant;
import java.util.UUID;

public record MissionCreatedEvent(
    UUID missionId,
    String type,
    Instant occurredAt,
    String titulo,
    TipoAmeaca tipoAmeaca,
    PrioridadeMissao prioridade,
    MissionStatus status) {}
