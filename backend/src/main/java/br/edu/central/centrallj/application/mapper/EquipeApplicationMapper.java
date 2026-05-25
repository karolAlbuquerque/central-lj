package br.edu.central.centrallj.application.mapper;

import br.edu.central.centrallj.application.model.EquipeView;
import br.edu.central.centrallj.domain.EquipeHeroica;
import org.springframework.stereotype.Component;

@Component
public class EquipeApplicationMapper {

  public EquipeView toView(EquipeHeroica e) {
    return new EquipeView(
        e.getId(),
        e.getNome(),
        e.getEspecialidadePrincipal(),
        e.isAtiva(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
