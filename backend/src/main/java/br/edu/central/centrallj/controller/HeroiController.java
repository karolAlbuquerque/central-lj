package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.dto.CreateHeroRequest;
import br.edu.central.centrallj.dto.HeroDetailResponse;
import br.edu.central.centrallj.dto.HeroResponse;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.dto.PatchHeroAvailabilityRequest;
import br.edu.central.centrallj.security.HeroiAccessPolicy;
import br.edu.central.centrallj.security.UsuarioPrincipal;
import br.edu.central.centrallj.service.HeroiService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heroes")
@Validated
public class HeroiController {

  private final HeroiService heroiService;
  private final HeroiAccessPolicy heroiAccessPolicy;

  public HeroiController(HeroiService heroiService, HeroiAccessPolicy heroiAccessPolicy) {
    this.heroiService = heroiService;
    this.heroiAccessPolicy = heroiAccessPolicy;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public ResponseEntity<HeroResponse> create(@Valid @RequestBody CreateHeroRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(heroiService.create(request));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<HeroResponse> list() {
    return heroiService.listAll();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public HeroDetailResponse getDetail(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return heroiService.getDetail(id);
  }

  @GetMapping("/{id}/missions")
  @PreAuthorize("isAuthenticated()")
  public List<MissionResponse> missions(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return heroiService.listMissionsForHero(id);
  }

  @PatchMapping("/{id}/availability")
  @PreAuthorize("isAuthenticated()")
  public HeroResponse patchAvailability(
      @PathVariable UUID id,
      @Valid @RequestBody PatchHeroAvailabilityRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return heroiService.patchAvailability(id, request);
  }
}
