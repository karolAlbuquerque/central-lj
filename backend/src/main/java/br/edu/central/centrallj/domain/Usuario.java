package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class Usuario {

  private UUID id;

  private String nome;

  private String email;

  private String senhaHash;

  private UserRole role;

  private boolean ativo = true;

  private Heroi heroi;

  private Instant cooldownAvailableAt;

  private Instant createdAt;

  private Instant updatedAt;

  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
    if (email != null) {
      email = email.trim().toLowerCase();
    }
  }

  void preUpdate() {
    updatedAt = Instant.now();
    if (email != null) {
      email = email.trim().toLowerCase();
    }
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getSenhaHash() {
    return senhaHash;
  }

  public void setSenhaHash(String senhaHash) {
    this.senhaHash = senhaHash;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public boolean isAtivo() {
    return ativo;
  }

  public void setAtivo(boolean ativo) {
    this.ativo = ativo;
  }

  public Heroi getHeroi() {
    return heroi;
  }

  public void setHeroi(Heroi heroi) {
    this.heroi = heroi;
  }

  public Instant getCooldownAvailableAt() {
    return cooldownAvailableAt;
  }

  public void setCooldownAvailableAt(Instant cooldownAvailableAt) {
    this.cooldownAvailableAt = cooldownAvailableAt;
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