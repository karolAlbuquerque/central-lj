package br.edu.central.centrallj.application.model;

import java.time.Instant;
import java.util.UUID;

public record MissionAssignmentView(
    UUID heroiId,
    String heroiNome,
    UUID equipeId,
    String equipeNome,
    Instant atribuidoEm,
    String atribuidoPor) {}
