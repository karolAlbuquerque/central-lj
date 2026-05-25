package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class Mission {

  private UUID id;

  private String titulo;

  private String descricao;

  private TipoAmeaca tipoAmeaca;

  private PrioridadeMissao prioridade;

  private MissionStatus status;

  private Instant dataCriacao;

  private Instant ultimaAtualizacao;

  private String cidade;

  private String bairro;

  private String referencia;

  private Heroi heroiResponsavel;

  private EquipeHeroica equipeResponsavel;

  private Instant atribuidoEm;

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