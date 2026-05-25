package br.edu.central.centrallj.application.mapper;

import br.edu.central.centrallj.application.model.HeroView;
import br.edu.central.centrallj.domain.Heroi;
import org.springframework.stereotype.Component;

@Component
public class HeroApplicationMapper {

  public HeroView toView(Heroi h) {
    return new HeroView(
        h.getId(),
        h.getNomeHeroico(),
        h.getNomeCivil(),
        h.getEspecialidade(),
        h.getStatusDisponibilidade(),
        h.getNivel(),
        h.isAtivo(),
        h.getEquipe() != null ? h.getEquipe().getId() : null,
        h.getEquipe() != null ? h.getEquipe().getNome() : null,
        h.getCreatedAt(),
        h.getUpdatedAt());
  }
}
