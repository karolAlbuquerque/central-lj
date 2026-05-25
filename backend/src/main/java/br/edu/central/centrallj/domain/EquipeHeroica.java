package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class EquipeHeroica {

  private UUID id;

  private String nome;

  private String especialidadePrincipal;

  private boolean ativa = true;

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

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getEspecialidadePrincipal() {
    return especialidadePrincipal;
  }

  public void setEspecialidadePrincipal(String especialidadePrincipal) {
    this.especialidadePrincipal = especialidadePrincipal;
  }

  public boolean isAtiva() {
    return ativa;
  }

  public void setAtiva(boolean ativa) {
    this.ativa = ativa;
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