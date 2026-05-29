package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.DuelSessionResponse;
import br.edu.central.centrallj.adapter.in.web.dto.PlayerMissionResponse;
import br.edu.central.centrallj.adapter.in.web.security.UsuarioPrincipal;
import br.edu.central.centrallj.application.model.DuelSessionView;
import br.edu.central.centrallj.application.model.PlayerMissionView;
import br.edu.central.centrallj.application.port.in.ListVillainTargetsUseCase;
import br.edu.central.centrallj.application.port.in.StartInfiltrationUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/villain")
public class VillainController {

  private final ListVillainTargetsUseCase listTargetsUseCase;
  private final StartInfiltrationUseCase startInfiltrationUseCase;

  public VillainController(
      ListVillainTargetsUseCase listTargetsUseCase,
      StartInfiltrationUseCase startInfiltrationUseCase) {
    this.listTargetsUseCase = listTargetsUseCase;
    this.startInfiltrationUseCase = startInfiltrationUseCase;
  }

  @GetMapping("/targets")
  @PreAuthorize("hasRole('VILLAIN')")
  public List<PlayerMissionResponse> listTargets() {
    return listTargetsUseCase.listTargets().stream().map(this::toDto).toList();
  }

  @PostMapping("/infiltrate/{missionId}")
  @PreAuthorize("hasRole('VILLAIN')")
  public ResponseEntity<DuelSessionResponse> infiltrate(
      @PathVariable UUID missionId, @AuthenticationPrincipal UsuarioPrincipal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(toDuelDto(startInfiltrationUseCase.start(missionId, principal.id())));
  }

  private PlayerMissionResponse toDto(PlayerMissionView v) {
    return new PlayerMissionResponse(
        v.id(), v.titulo(), v.descricao(), v.ownerUserId(),
        v.combatState(), v.minPlayers(), v.partySeed(), v.startedByUserId(),
        v.startedAt(), v.createdAt(), v.updatedAt());
  }

  private DuelSessionResponse toDuelDto(DuelSessionView v) {
    return new DuelSessionResponse(
        v.id(), v.missionId(), v.attackerUserId(), v.defenderUserId(),
        v.seed(), v.puzzleType(), v.status(),
        v.roundCurrent(), v.roundMax(),
        v.attackerRoundsWon(), v.defenderRoundsWon(),
        v.infiltrationProgress(), v.infiltrationRequired(),
        v.startedAt(), v.finishedAt(), v.timeoutAt());
  }
}
