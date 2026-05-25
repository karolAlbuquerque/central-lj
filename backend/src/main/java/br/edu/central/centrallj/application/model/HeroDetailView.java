package br.edu.central.centrallj.application.model;

import java.util.List;

public record HeroDetailView(HeroView heroi, List<MissionView> missoes) {}
