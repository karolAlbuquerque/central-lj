package br.edu.central.centrallj.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record EquipeResponse(
    UUID id,
    String nome,
    String especialidadePrincipal,
    boolean ativa,
    Instant createdAt,
    Instant updatedAt) {}
