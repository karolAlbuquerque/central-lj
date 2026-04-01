package br.edu.central.centrallj;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import br.edu.central.centrallj.repository.MissionHistoryRepository;
import br.edu.central.centrallj.repository.MissionRepository;
import br.edu.central.centrallj.service.MissionWorkflowService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MissionWorkflowIntegrationTest {

  @Autowired private MissionRepository missionRepository;
  @Autowired private MissionHistoryRepository missionHistoryRepository;
  @Autowired private MissionWorkflowService missionWorkflowService;

  @Test
  void workflowEvoluiDaRecebidaAteConcluidaERegistraHistorico() {
    Mission m = new Mission();
    m.setId(UUID.randomUUID());
    m.setTitulo("WF test");
    m.setDescricao(null);
    m.setTipoAmeaca(TipoAmeaca.INVASAO);
    m.setPrioridade(PrioridadeMissao.BAIXA);
    m.setStatus(MissionStatus.RECEBIDA);
    Instant now = Instant.now();
    m.setDataCriacao(now);
    m.setUltimaAtualizacao(now);
    missionRepository.save(m);

    missionWorkflowService.processAfterCreation(m.getId());

    Mission updated = missionRepository.findById(m.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(MissionStatus.CONCLUIDA);

    var hist = missionHistoryRepository.findByMission_IdOrderByOcorridoEmAsc(m.getId());
    assertThat(hist).hasSize(5);
    assertThat(hist.get(hist.size() - 1).getStatusNovo()).isEqualTo(MissionStatus.CONCLUIDA);
  }

  @Test
  void markFailureDefineStatusEHistorico() {
    Mission m = new Mission();
    m.setId(UUID.randomUUID());
    m.setTitulo("Falha");
    m.setTipoAmeaca(TipoAmeaca.TECNOLOGICA);
    m.setPrioridade(PrioridadeMissao.MEDIA);
    m.setStatus(MissionStatus.EM_ANALISE);
    Instant now = Instant.now();
    m.setDataCriacao(now);
    m.setUltimaAtualizacao(now);
    missionRepository.save(m);

    missionWorkflowService.markFailure(m.getId());

    Mission updated = missionRepository.findById(m.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(MissionStatus.FALHA_PROCESSAMENTO);
    var hist = missionHistoryRepository.findByMission_IdOrderByOcorridoEmAsc(m.getId());
    assertThat(hist).hasSize(1);
    assertThat(hist.get(0).getStatusNovo()).isEqualTo(MissionStatus.FALHA_PROCESSAMENTO);
  }

  @Test
  void workflowPrioridadeCriticaPulaAnalise() {
    Mission m = new Mission();
    m.setId(UUID.randomUUID());
    m.setTitulo("WF crítico");
    m.setTipoAmeaca(TipoAmeaca.TECNOLOGICA);
    m.setPrioridade(PrioridadeMissao.CRITICA);
    m.setStatus(MissionStatus.RECEBIDA);
    Instant now = Instant.now();
    m.setDataCriacao(now);
    m.setUltimaAtualizacao(now);
    missionRepository.save(m);

    missionWorkflowService.processAfterCreation(m.getId());

    var hist = missionHistoryRepository.findByMission_IdOrderByOcorridoEmAsc(m.getId());
    assertThat(hist.get(0).getStatusNovo()).isEqualTo(MissionStatus.PRIORIZADA);
  }
}
