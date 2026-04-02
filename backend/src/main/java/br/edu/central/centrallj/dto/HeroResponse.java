package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import java.time.Instant;
import java.util.UUID;

public record HeroResponse(
    UUID id,
    String nomeHeroico,
    String nomeCivil,
    String especialidade,
    HeroiDisponibilidade statusDisponibilidade,
    String nivel,
    boolean ativo,
    EquipeResumo equipe,
    Instant createdAt,
    Instant updatedAt) {

  public record EquipeResumo(UUID id, String nome) {}
}
