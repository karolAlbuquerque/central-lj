package br.edu.central.centrallj.application.model;

import java.util.UUID;

public record CreatePlayerMissionCommand(
    String titulo,
    String descricao,
    Integer minPlayers,
    UUID ownerUserId) {}
