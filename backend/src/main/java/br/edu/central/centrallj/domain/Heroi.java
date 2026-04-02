package br.edu.central.centrallj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "heroes")
public class Heroi {

  @Id private UUID id;

  @Column(name = "nome_heroico", nullable = false, length = 200)
  private String nomeHeroico;

  @Column(name = "nome_civil", length = 200)
  private String nomeCivil;

  @Column(nullable = false, length = 200)
  private String especialidade;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_disponibilidade", nullable = false, length = 50)
  private HeroiDisponibilidade statusDisponibilidade = HeroiDisponibilidade.DISPONIVEL;

  @Column(nullable = false, length = 50)
  private String nivel;

  @Column(nullable = false)
  private boolean ativo = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "equipe_id")
  private EquipeHeroica equipe;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
  }

  @PreUpdate
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
