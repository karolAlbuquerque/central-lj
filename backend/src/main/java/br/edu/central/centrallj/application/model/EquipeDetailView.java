package br.edu.central.centrallj.application.model;

import java.util.List;

public record EquipeDetailView(EquipeView equipe, List<HeroView> herois) {}
