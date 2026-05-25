package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import java.time.Instant;
import java.util.UUID;

public record HeroView(
    UUID id,
    String nomeHeroico,
    String nomeCivil,
    String especialidade,
    HeroiDisponibilidade statusDisponibilidade,
    String nivel,
    boolean ativo,
    UUID equipeId,
    String equipeNome,
    Instant createdAt,
    Instant updatedAt) {}
