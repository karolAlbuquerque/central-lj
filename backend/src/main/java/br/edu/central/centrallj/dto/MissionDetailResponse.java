package br.edu.central.centrallj.dto;

import java.util.List;

public record MissionDetailResponse(MissionResponse missao, List<MissionHistoryEntryResponse> historico) {}
