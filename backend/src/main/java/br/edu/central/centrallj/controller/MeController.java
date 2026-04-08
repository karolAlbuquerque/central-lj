package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.domain.UserRole;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.security.UsuarioPrincipal;
import br.edu.central.centrallj.application.port.in.heroes.HeroUseCase;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

  private final HeroUseCase heroUseCase;

  public MeController(HeroUseCase heroUseCase) {
    this.heroUseCase = heroUseCase;
  }

  /**
   * Missões em que o usuário HERO é o responsável atual. ADMIN/OPERATOR recebem lista vazia (use
   * painel administrativo).
   */
  @GetMapping("/missions")
  public ResponseEntity<List<MissionResponse>> myMissions(
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (principal.role() == UserRole.HERO && principal.heroiId() != null) {
      return ResponseEntity.ok(heroUseCase.listMissionsForHero(principal.heroiId()));
    }
    return ResponseEntity.ok(List.of());
  }
}
