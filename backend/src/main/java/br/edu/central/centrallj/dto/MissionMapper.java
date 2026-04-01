package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistory;
import org.springframework.stereotype.Component;

@Component
public class MissionMapper {

  public MissionResponse toResponse(Mission m) {
    MissionResponse.LocalizacaoResponse loc = null;
    if (m.getCidade() != null || m.getBairro() != null || m.getReferencia() != null) {
      loc = new MissionResponse.LocalizacaoResponse(m.getCidade(), m.getBairro(), m.getReferencia());
    }
    return new MissionResponse(
        m.getId(),
        m.getTitulo(),
        m.getDescricao(),
        m.getTipoAmeaca(),
        m.getPrioridade(),
        m.getStatus(),
        m.getDataCriacao(),
        m.getUltimaAtualizacao(),
        loc);
  }

  public MissionHistoryEntryResponse toHistoryEntry(MissionHistory h) {
    return new MissionHistoryEntryResponse(
        h.getId(),
        h.getStatusAnterior(),
        h.getStatusNovo(),
        h.getMensagem(),
        h.getOrigem(),
        h.getOcorridoEm());
  }
}
