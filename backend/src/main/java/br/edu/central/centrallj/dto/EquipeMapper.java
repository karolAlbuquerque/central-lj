package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.EquipeHeroica;
import org.springframework.stereotype.Component;

@Component
public class EquipeMapper {

  public EquipeResponse toResponse(EquipeHeroica e) {
    return new EquipeResponse(
        e.getId(),
        e.getNome(),
        e.getEspecialidadePrincipal(),
        e.isAtiva(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
