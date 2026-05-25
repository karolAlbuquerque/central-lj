package br.edu.central.centrallj.application.model;

import java.time.Instant;
import java.util.UUID;

public record EquipeView(
    UUID id,
    String nome,
    String especialidadePrincipal,
    boolean ativa,
    Instant createdAt,
    Instant updatedAt) {}
