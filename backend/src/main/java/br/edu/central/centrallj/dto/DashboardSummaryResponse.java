package br.edu.central.centrallj.dto;

import java.util.List;

public record DashboardSummaryResponse(
    long totalMissaoes,
    long emAndamento,
    long concluidas,
    long falhas,
    List<MissionResponse> recentes) {}
