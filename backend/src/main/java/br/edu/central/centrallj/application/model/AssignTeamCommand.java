package br.edu.central.centrallj.application.model;

import java.util.UUID;

public record AssignTeamCommand(UUID equipeId, String atribuidoPor) {}
