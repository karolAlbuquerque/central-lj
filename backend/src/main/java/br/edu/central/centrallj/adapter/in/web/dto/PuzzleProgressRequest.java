package br.edu.central.centrallj.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PuzzleProgressRequest(
    @NotNull List<Integer> moves,
    int roundNumber,
    long timeMs) {}
