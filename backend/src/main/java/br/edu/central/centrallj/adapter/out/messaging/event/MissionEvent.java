package br.edu.central.centrallj.adapter.out.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record MissionEvent(
    UUID eventId,
    UUID missionId,
    String type,
    String status,
    Instant timestamp,
    String title
) {}

