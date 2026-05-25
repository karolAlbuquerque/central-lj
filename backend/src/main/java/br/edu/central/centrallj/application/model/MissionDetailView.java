package br.edu.central.centrallj.application.model;

import java.util.List;

public record MissionDetailView(MissionView missao, List<MissionHistoryEntryView> historico) {}
