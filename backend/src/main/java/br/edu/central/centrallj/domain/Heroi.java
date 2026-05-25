package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class Heroi {

  private UUID id;

  private String nomeHeroico;

  private String nomeCivil;

  private String especialidade;

  private HeroiDisponibilidade statusDisponibilidade = HeroiDisponibilidade.DISPONIVEL;

  private String nivel;

  private boolean ativo = true;

  private EquipeHeroica equipe;

  private Instant createdAt;

  private Instant updatedAt;

  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
  }

  void preUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getNomeHeroico() {
    return nomeHeroico;
  }

  public void setNomeHeroico(String nomeHeroico) {
    this.nomeHeroico = nomeHeroico;
  }

  public String getNomeCivil() {
    return nomeCivil;
  }

  public void setNomeCivil(String nomeCivil) {
    this.nomeCivil = nomeCivil;
  }

  public String getEspecialidade() {
    return especialidade;
  }

  public void setEspecialidade(String especialidade) {
    this.especialidade = especialidade;
  }

  public HeroiDisponibilidade getStatusDisponibilidade() {
    return statusDisponibilidade;
  }

  public void setStatusDisponibilidade(HeroiDisponibilidade statusDisponibilidade) {
    this.statusDisponibilidade = statusDisponibilidade;
  }

  public String getNivel() {
    return nivel;
  }

  public void setNivel(String nivel) {
    this.nivel = nivel;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void setAtivo(boolean ativo) {
    this.ativo = ativo;
  }

  public EquipeHeroica getEquipe() {
    return equipe;
  }

  public void setEquipe(EquipeHeroica equipe) {
    this.equipe = equipe;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}