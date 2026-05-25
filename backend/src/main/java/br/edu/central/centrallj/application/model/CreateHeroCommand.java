package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import java.util.UUID;

public record CreateHeroCommand(
    String nomeHeroico,
    String nomeCivil,
    String especialidade,
    HeroiDisponibilidade statusDisponibilidade,
    String nivel,
    boolean ativo,
    UUID equipeId) {}
