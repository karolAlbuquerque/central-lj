package br.edu.central.centrallj;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.central.centrallj.application.port.in.ProcessMissionCreatedUseCase;
import br.edu.central.centrallj.application.port.out.MissionHistoryPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionPersistencePort;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MissionWorkflowIntegrationTest {

  @Autowired private MissionPersistencePort missionPersistencePort;
  @Autowired private MissionHistoryPersistencePort missionHistoryPersistencePort;
  @Autowired private ProcessMissionCreatedUseCase processMissionCreatedUseCase;

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
    missionPersistencePort.save(m);

    processMissionCreatedUseCase.processAfterCreation(m.getId());

    Mission updated =
        missionPersistencePort.findById(m.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(MissionStatus.CONCLUIDA);

    var historico = missionHistoryPersistencePort.findByMissionIdOrderByOcorridoEmAsc(m.getId());
    assertThat(historico).isNotEmpty();
    assertThat(historico.get(historico.size() - 1).getStatusNovo())
        .isEqualTo(MissionStatus.CONCLUIDA);
  }
}
