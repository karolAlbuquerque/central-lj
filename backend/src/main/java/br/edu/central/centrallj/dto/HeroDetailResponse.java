package br.edu.central.centrallj.dto;

import java.util.List;

public record HeroDetailResponse(HeroResponse heroi, List<MissionResponse> missoes) {}
