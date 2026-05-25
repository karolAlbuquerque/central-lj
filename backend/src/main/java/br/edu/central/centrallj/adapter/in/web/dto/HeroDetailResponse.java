package br.edu.central.centrallj.adapter.in.web.dto;

import java.util.List;

public record HeroDetailResponse(HeroResponse heroi, List<MissionResponse> missoes) {}
