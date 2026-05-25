package br.edu.central.centrallj.application.model;

import java.util.List;

public record DashboardSummaryView(
    long total, long emAndamento, long concluidas, long falhas, List<MissionView> recentes) {}
