package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.DuelStatus;
import br.edu.central.centrallj.domain.PuzzleType;
import java.time.Instant;
import java.util.UUID;

public record DuelSessionView(
    UUID id,
    UUID missionId,
    UUID attackerUserId,
    UUID defenderUserId,
    String seed,
    PuzzleType puzzleType,
    DuelStatus status,
    int roundCurrent,
    int roundMax,
    int attackerRoundsWon,
    int defenderRoundsWon,
    int infiltrationProgress,
    int infiltrationRequired,
    Instant startedAt,
    Instant finishedAt,
    Instant timeoutAt) {}
