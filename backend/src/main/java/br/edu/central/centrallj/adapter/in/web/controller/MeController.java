package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.MissionResponse;
import br.edu.central.centrallj.adapter.in.web.mapper.WebDtoMapper;
import br.edu.central.centrallj.adapter.in.web.security.UsuarioPrincipal;
import br.edu.central.centrallj.application.port.in.ManageHeroiUseCase;
import br.edu.central.centrallj.domain.UserRole;
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

  private final ManageHeroiUseCase manageHeroiUseCase;
  private final WebDtoMapper webDtoMapper;

  public MeController(ManageHeroiUseCase manageHeroiUseCase, WebDtoMapper webDtoMapper) {
    this.manageHeroiUseCase = manageHeroiUseCase;
    this.webDtoMapper = webDtoMapper;
  }

  @GetMapping("/missions")
  public ResponseEntity<List<MissionResponse>> myMissions(
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    if (principal.role() == UserRole.HERO && principal.heroiId() != null) {
      return ResponseEntity.ok(
          manageHeroiUseCase.listMissionsForHero(principal.heroiId()).stream()
              .map(webDtoMapper::toDto)
              .toList());
    }
    return ResponseEntity.ok(List.of());
  }
}
