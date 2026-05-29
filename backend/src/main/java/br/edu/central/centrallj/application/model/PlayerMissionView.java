package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.MissionCombatState;
import java.time.Instant;
import java.util.UUID;

public record PlayerMissionView(
    UUID id,
    String titulo,
    String descricao,
    UUID ownerUserId,
    MissionCombatState combatState,
    Integer minPlayers,
    String partySeed,
    UUID startedByUserId,
    Instant startedAt,
    Instant createdAt,
    Instant updatedAt) {}
