package br.edu.central.centrallj.adapter.in.web.dto;

import java.util.List;

public record MissionDetailResponse(MissionResponse missao, List<MissionHistoryEntryResponse> historico) {}
