package br.edu.central.centrallj.dto;

import java.util.List;

public record EquipeDetailResponse(EquipeResponse equipe, List<HeroResponse> membros) {}
