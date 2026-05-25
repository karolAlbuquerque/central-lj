package br.edu.central.centrallj.adapter.in.web.dto;

import java.util.List;

public record EquipeDetailResponse(EquipeResponse equipe, List<HeroResponse> membros) {}
