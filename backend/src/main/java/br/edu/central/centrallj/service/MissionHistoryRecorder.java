package br.edu.central.centrallj.service;

import br.edu.central.centrallj.application.port.out.MissionHistoryPersistencePort;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistory;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.domain.MissionStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MissionHistoryRecorder {

  private final MissionHistoryPersistencePort missionHistoryPersistencePort;

  public MissionHistoryRecorder(MissionHistoryPersistencePort missionHistoryPersistencePort) {
    this.missionHistoryPersistencePort = missionHistoryPersistencePort;
  }

  public void record(
      Mission mission,
      MissionStatus statusAnterior,
      MissionStatus statusNovo,
      String mensagem,
      MissionHistoryOrigin origem) {
    MissionHistory row = new MissionHistory();
    row.setId(UUID.randomUUID());
    row.setMission(mission);
    row.setStatusAnterior(statusAnterior);
    row.setStatusNovo(statusNovo);
    row.setMensagem(mensagem);
    row.setOrigem(origem);
    row.setOcorridoEm(Instant.now());
    missionHistoryPersistencePort.save(row);
  }
}
