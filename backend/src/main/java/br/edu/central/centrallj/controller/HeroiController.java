package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.application.port.in.ManageHeroiUseCase;
import br.edu.central.centrallj.dto.CreateHeroRequest;
import br.edu.central.centrallj.dto.HeroDetailResponse;
import br.edu.central.centrallj.dto.HeroResponse;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.dto.PatchHeroAvailabilityRequest;
import br.edu.central.centrallj.security.HeroiAccessPolicy;
import br.edu.central.centrallj.security.UsuarioPrincipal;
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

  private final ManageHeroiUseCase manageHeroiUseCase;
  private final HeroiAccessPolicy heroiAccessPolicy;

  public HeroiController(ManageHeroiUseCase manageHeroiUseCase, HeroiAccessPolicy heroiAccessPolicy) {
    this.manageHeroiUseCase = manageHeroiUseCase;
    this.heroiAccessPolicy = heroiAccessPolicy;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public ResponseEntity<HeroResponse> create(@Valid @RequestBody CreateHeroRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(manageHeroiUseCase.create(request));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<HeroResponse> list() {
    return manageHeroiUseCase.listAll();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public HeroDetailResponse getDetail(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return manageHeroiUseCase.getDetail(id);
  }

  @GetMapping("/{id}/missions")
  @PreAuthorize("isAuthenticated()")
  public List<MissionResponse> missions(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return manageHeroiUseCase.listMissionsForHero(id);
  }

  @PatchMapping("/{id}/availability")
  @PreAuthorize("isAuthenticated()")
  public HeroResponse patchAvailability(
      @PathVariable UUID id,
      @Valid @RequestBody PatchHeroAvailabilityRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return manageHeroiUseCase.patchAvailability(id, request);
  }
}
