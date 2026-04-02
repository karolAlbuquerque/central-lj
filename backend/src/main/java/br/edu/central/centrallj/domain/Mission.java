package br.edu.central.centrallj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "missions")
public class Mission {

  @Id private UUID id;

  @Column(nullable = false, length = 500)
  private String titulo;

  @Column(columnDefinition = "text")
  private String descricao;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_ameaca", nullable = false, length = 50)
  private TipoAmeaca tipoAmeaca;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private PrioridadeMissao prioridade;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private MissionStatus status;

  @Column(name = "data_criacao", nullable = false)
  private Instant dataCriacao;

  @Column(name = "ultima_atualizacao", nullable = false)
  private Instant ultimaAtualizacao;

  @Column(length = 200)
  private String cidade;

  @Column(length = 200)
  private String bairro;

  @Column(length = 500)
  private String referencia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "heroi_responsavel_id")
  private Heroi heroiResponsavel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "equipe_responsavel_id")
  private EquipeHeroica equipeResponsavel;

  @Column(name = "atribuido_em")
  private Instant atribuidoEm;

  @Column(name = "atribuido_por", length = 200)
  private String atribuidoPor;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public TipoAmeaca getTipoAmeaca() {
    return tipoAmeaca;
  }

  public void setTipoAmeaca(TipoAmeaca tipoAmeaca) {
    this.tipoAmeaca = tipoAmeaca;
  }

  public PrioridadeMissao getPrioridade() {
    return prioridade;
  }

  public void setPrioridade(PrioridadeMissao prioridade) {
    this.prioridade = prioridade;
  }

  public MissionStatus getStatus() {
    return status;
  }

  public void setStatus(MissionStatus status) {
    this.status = status;
  }

  public Instant getDataCriacao() {
    return dataCriacao;
  }

  public void setDataCriacao(Instant dataCriacao) {
    this.dataCriacao = dataCriacao;
  }

  public Instant getUltimaAtualizacao() {
    return ultimaAtualizacao;
  }

  public void setUltimaAtualizacao(Instant ultimaAtualizacao) {
    this.ultimaAtualizacao = ultimaAtualizacao;
  }

  public String getCidade() {
    return cidade;
  }

  public void setCidade(String cidade) {
    this.cidade = cidade;
  }

  public String getBairro() {
    return bairro;
  }

  public void setBairro(String bairro) {
    this.bairro = bairro;
  }

  public String getReferencia() {
    return referencia;
  }

  public void setReferencia(String referencia) {
    this.referencia = referencia;
  }

  public Heroi getHeroiResponsavel() {
    return heroiResponsavel;
  }

  public void setHeroiResponsavel(Heroi heroiResponsavel) {
    this.heroiResponsavel = heroiResponsavel;
  }

  public EquipeHeroica getEquipeResponsavel() {
    return equipeResponsavel;
  }

  public void setEquipeResponsavel(EquipeHeroica equipeResponsavel) {
    this.equipeResponsavel = equipeResponsavel;
  }

  public Instant getAtribuidoEm() {
    return atribuidoEm;
  }

  public void setAtribuidoEm(Instant atribuidoEm) {
    this.atribuidoEm = atribuidoEm;
  }

  public String getAtribuidoPor() {
    return atribuidoPor;
  }

  public void setAtribuidoPor(String atribuidoPor) {
    this.atribuidoPor = atribuidoPor;
  }
}
