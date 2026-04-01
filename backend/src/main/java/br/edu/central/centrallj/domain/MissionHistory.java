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
@Table(name = "mission_history")
public class MissionHistory {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_anterior", length = 50)
  private MissionStatus statusAnterior;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_novo", nullable = false, length = 50)
  private MissionStatus statusNovo;

  @Column(columnDefinition = "text")
  private String mensagem;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private MissionHistoryOrigin origem;

  @Column(name = "ocorrido_em", nullable = false)
  private Instant ocorridoEm;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Mission getMission() {
    return mission;
  }

  public void setMission(Mission mission) {
    this.mission = mission;
  }

  public MissionStatus getStatusAnterior() {
    return statusAnterior;
  }

  public void setStatusAnterior(MissionStatus statusAnterior) {
    this.statusAnterior = statusAnterior;
  }

  public MissionStatus getStatusNovo() {
    return statusNovo;
  }

  public void setStatusNovo(MissionStatus statusNovo) {
    this.statusNovo = statusNovo;
  }

  public String getMensagem() {
    return mensagem;
  }

  public void setMensagem(String mensagem) {
    this.mensagem = mensagem;
  }

  public MissionHistoryOrigin getOrigem() {
    return origem;
  }

  public void setOrigem(MissionHistoryOrigin origem) {
    this.origem = origem;
  }

  public Instant getOcorridoEm() {
    return ocorridoEm;
  }

  public void setOcorridoEm(Instant ocorridoEm) {
    this.ocorridoEm = ocorridoEm;
  }
}
