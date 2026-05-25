package br.edu.central.centrallj.application.model;

import java.util.UUID;

public record AssignHeroCommand(UUID heroiId, String atribuidoPor) {}
