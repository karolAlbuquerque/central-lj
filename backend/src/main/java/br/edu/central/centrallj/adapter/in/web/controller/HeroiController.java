package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.CreateHeroRequest;
import br.edu.central.centrallj.adapter.in.web.dto.HeroDetailResponse;
import br.edu.central.centrallj.adapter.in.web.dto.HeroResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionResponse;
import br.edu.central.centrallj.adapter.in.web.dto.PatchHeroAvailabilityRequest;
import br.edu.central.centrallj.adapter.in.web.mapper.WebDtoMapper;
import br.edu.central.centrallj.adapter.in.web.security.HeroiAccessPolicy;
import br.edu.central.centrallj.adapter.in.web.security.UsuarioPrincipal;
import br.edu.central.centrallj.application.port.in.ManageHeroiUseCase;
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
  private final WebDtoMapper webDtoMapper;

  public HeroiController(
      ManageHeroiUseCase manageHeroiUseCase,
      HeroiAccessPolicy heroiAccessPolicy,
      WebDtoMapper webDtoMapper) {
    this.manageHeroiUseCase = manageHeroiUseCase;
    this.heroiAccessPolicy = heroiAccessPolicy;
    this.webDtoMapper = webDtoMapper;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public ResponseEntity<HeroResponse> create(@Valid @RequestBody CreateHeroRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(webDtoMapper.toDto(manageHeroiUseCase.create(webDtoMapper.toCommand(request))));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<HeroResponse> list() {
    return manageHeroiUseCase.listAll().stream().map(webDtoMapper::toDto).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public HeroDetailResponse getDetail(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return webDtoMapper.toDto(manageHeroiUseCase.getDetail(id));
  }

  @GetMapping("/{id}/missions")
  @PreAuthorize("isAuthenticated()")
  public List<MissionResponse> missions(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return manageHeroiUseCase.listMissionsForHero(id).stream().map(webDtoMapper::toDto).toList();
  }

  @PatchMapping("/{id}/availability")
  @PreAuthorize("isAuthenticated()")
  public HeroResponse patchAvailability(
      @PathVariable UUID id,
      @Valid @RequestBody PatchHeroAvailabilityRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    heroiAccessPolicy.assertManageOrSelf(principal, id);
    return webDtoMapper.toDto(
        manageHeroiUseCase.patchAvailability(id, webDtoMapper.toCommand(request)));
  }
}
