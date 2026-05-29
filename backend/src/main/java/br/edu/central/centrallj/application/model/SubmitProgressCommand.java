package br.edu.central.centrallj.application.model;

import java.util.List;
import java.util.UUID;

public record SubmitProgressCommand(UUID duelId, UUID userId, int roundNumber, List<Integer> moves, long timeMs) {}
