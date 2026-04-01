package br.edu.central.centrallj.messaging.event;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import java.time.Instant;
import java.util.UUID;

/** Payload publicado no tópico {@code missions.created} após persistir a missão. */
public record MissionCreatedKafkaEvent(
    UUID eventId,
    String type,
    UUID missionId,
    String titulo,
    TipoAmeaca tipoAmeaca,
    PrioridadeMissao prioridade,
    MissionStatus status,
    Instant occurredAt) {}
