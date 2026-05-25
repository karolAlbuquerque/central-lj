package br.edu.central.centrallj.domain;

import java.time.Instant;
import java.util.UUID;

public class MissionHistory {

  private UUID id;

  private UUID missionId;

  private MissionStatus statusAnterior;

  private MissionStatus statusNovo;

  private String mensagem;

  private MissionHistoryOrigin origem;

  private Instant ocorridoEm;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public java.util.UUID getMissionId() { return missionId; }

  public void setMissionId(java.util.UUID missionId) { this.missionId = missionId; }

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