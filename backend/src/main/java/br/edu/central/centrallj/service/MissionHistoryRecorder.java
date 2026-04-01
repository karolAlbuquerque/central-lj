package br.edu.central.centrallj.service;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistory;
import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.repository.MissionHistoryRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Persistência do histórico de transições de status (rastreabilidade do fluxo assíncrono e da API).
 */
@Service
public class MissionHistoryRecorder {

  private final MissionHistoryRepository missionHistoryRepository;

  public MissionHistoryRecorder(MissionHistoryRepository missionHistoryRepository) {
    this.missionHistoryRepository = missionHistoryRepository;
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
    missionHistoryRepository.save(row);
  }
}
