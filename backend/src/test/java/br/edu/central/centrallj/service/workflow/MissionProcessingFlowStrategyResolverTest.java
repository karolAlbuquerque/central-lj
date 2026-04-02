package br.edu.central.centrallj.service.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import org.junit.jupiter.api.Test;

class MissionProcessingFlowStrategyResolverTest {

  @Test
  void prioridadeCriticaUsaFluxoCritico() {
    DefaultMissionProcessingFlowStrategy def = new DefaultMissionProcessingFlowStrategy();
    CriticalPriorityMissionProcessingFlowStrategy crit = new CriticalPriorityMissionProcessingFlowStrategy();
    MissionProcessingFlowStrategyResolver resolver = new MissionProcessingFlowStrategyResolver(def, crit);

    Mission m = new Mission();
    m.setPrioridade(PrioridadeMissao.CRITICA);

    assertThat(resolver.resolve(m)).isSameAs(crit);
  }

  @Test
  void prioridadeNaoCriticaUsaFluxoPadrao() {
    DefaultMissionProcessingFlowStrategy def = new DefaultMissionProcessingFlowStrategy();
    CriticalPriorityMissionProcessingFlowStrategy crit = new CriticalPriorityMissionProcessingFlowStrategy();
    MissionProcessingFlowStrategyResolver resolver = new MissionProcessingFlowStrategyResolver(def, crit);

    Mission m = new Mission();
    m.setPrioridade(PrioridadeMissao.MEDIA);

    assertThat(resolver.resolve(m)).isSameAs(def);
  }
}
