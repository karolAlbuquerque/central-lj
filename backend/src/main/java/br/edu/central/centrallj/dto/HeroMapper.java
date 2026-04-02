package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.Heroi;
import org.springframework.stereotype.Component;

@Component
public class HeroMapper {

  public HeroResponse toResponse(Heroi h) {
    HeroResponse.EquipeResumo equipe = null;
    if (h.getEquipe() != null) {
      equipe = new HeroResponse.EquipeResumo(h.getEquipe().getId(), h.getEquipe().getNome());
    }
    return new HeroResponse(
        h.getId(),
        h.getNomeHeroico(),
        h.getNomeCivil(),
        h.getEspecialidade(),
        h.getStatusDisponibilidade(),
        h.getNivel(),
        h.isAtivo(),
        equipe,
        h.getCreatedAt(),
        h.getUpdatedAt());
  }
}
