package br.edu.central.centrallj.application.mapper;

import br.edu.central.centrallj.application.model.MissionAssignmentView;
import br.edu.central.centrallj.application.model.MissionDetailView;
import br.edu.central.centrallj.application.model.MissionHistoryEntryView;
import br.edu.central.centrallj.application.model.MissionLocationView;
import br.edu.central.centrallj.application.model.MissionView;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistory;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MissionApplicationMapper {

  public MissionView toView(Mission m) {
    MissionLocationView loc = null;
    if (m.getCidade() != null || m.getBairro() != null || m.getReferencia() != null) {
      loc = new MissionLocationView(m.getCidade(), m.getBairro(), m.getReferencia());
    }
    return new MissionView(
        m.getId(),
        m.getTitulo(),
        m.getDescricao(),
        m.getTipoAmeaca(),
        m.getPrioridade(),
        m.getStatus(),
        m.getDataCriacao(),
        m.getUltimaAtualizacao(),
        loc,
        buildAssignment(m));
  }

  private MissionAssignmentView buildAssignment(Mission m) {
    if (m.getHeroiResponsavel() == null
        && m.getEquipeResponsavel() == null
        && m.getAtribuidoEm() == null) {
      return null;
    }
    var h = m.getHeroiResponsavel();
    var eq = m.getEquipeResponsavel();
    return new MissionAssignmentView(
        h != null ? h.getId() : null,
        h != null ? h.getNomeHeroico() : null,
        eq != null ? eq.getId() : null,
        eq != null ? eq.getNome() : null,
        m.getAtribuidoEm(),
        m.getAtribuidoPor());
  }

  public MissionHistoryEntryView toHistoryView(MissionHistory h) {
    return new MissionHistoryEntryView(
        h.getId(),
        h.getStatusAnterior(),
        h.getStatusNovo(),
        h.getMensagem(),
        h.getOrigem(),
        h.getOcorridoEm());
  }

  public MissionDetailView toDetailView(Mission mission, List<MissionHistory> history) {
    return new MissionDetailView(
        toView(mission), history.stream().map(this::toHistoryView).toList());
  }
}
