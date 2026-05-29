package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.DuelSessionResponse;
import br.edu.central.centrallj.adapter.in.web.dto.PuzzleProgressRequest;
import br.edu.central.centrallj.adapter.in.web.dto.SabotageChoiceRequest;
import br.edu.central.centrallj.adapter.in.web.security.UsuarioPrincipal;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.model.DuelSessionView;
import br.edu.central.centrallj.application.model.SubmitProgressCommand;
import br.edu.central.centrallj.application.port.in.JoinDuelUseCase;
import br.edu.central.centrallj.application.port.in.SubmitInfiltrationProgressUseCase;
import br.edu.central.centrallj.application.port.in.SubmitPuzzleProgressUseCase;
import br.edu.central.centrallj.application.port.out.DuelSessionPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionMemberPersistencePort;
import br.edu.central.centrallj.application.port.out.SabotageEventPersistencePort;
import br.edu.central.centrallj.domain.DuelSession;
import br.edu.central.centrallj.domain.DuelStatus;
import br.edu.central.centrallj.domain.SabotageEvent;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/duels")
@Validated
public class DuelController {

  private final JoinDuelUseCase joinDuelUseCase;
  private final SubmitInfiltrationProgressUseCase submitInfiltrationUseCase;
  private final SubmitPuzzleProgressUseCase submitProgressUseCase;
  private static final List<DuelStatus> MISSION_VISIBLE_DUEL_STATUSES =
      List.of(DuelStatus.PENDING, DuelStatus.ACTIVE);

  private final DuelSessionPersistencePort duelPort;
  private final SabotageEventPersistencePort sabotagePort;
  private final MissionMemberPersistencePort memberPort;

  public DuelController(
      JoinDuelUseCase joinDuelUseCase,
      SubmitInfiltrationProgressUseCase submitInfiltrationUseCase,
      SubmitPuzzleProgressUseCase submitProgressUseCase,
      DuelSessionPersistencePort duelPort,
      SabotageEventPersistencePort sabotagePort,
      MissionMemberPersistencePort memberPort) {
    this.joinDuelUseCase = joinDuelUseCase;
    this.submitInfiltrationUseCase = submitInfiltrationUseCase;
    this.submitProgressUseCase = submitProgressUseCase;
    this.duelPort = duelPort;
    this.sabotagePort = sabotagePort;
    this.memberPort = memberPort;
  }

  @GetMapping("/by-mission/{missionId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DuelSessionResponse> findActiveByMission(
      @PathVariable UUID missionId, @AuthenticationPrincipal UsuarioPrincipal principal) {
    DuelSession session =
        duelPort
            .findActiveByMission(missionId, MISSION_VISIBLE_DUEL_STATUSES)
            .orElse(null);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    UUID userId = principal.id();
    boolean allowed =
        session.getAttackerUserId().equals(userId)
            || session.getDefenderUserId().equals(userId)
            || memberPort.existsByMissionAndUser(missionId, userId);
    if (!allowed) {
      throw new br.edu.central.centrallj.application.exception.BadRequestException("Acesso negado.");
    }
    return ResponseEntity.ok(toDto(session));
  }

  @GetMapping("/{duelId}")
  @PreAuthorize("isAuthenticated()")
  public DuelSessionResponse getState(
      @PathVariable UUID duelId, @AuthenticationPrincipal UsuarioPrincipal principal) {
    return toDto(
        duelPort.findById(duelId)
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado.")));
  }

  @PostMapping("/{duelId}/infiltration-progress")
  @PreAuthorize("hasRole('VILLAIN')")
  public DuelSessionResponse submitInfiltrationProgress(
      @PathVariable UUID duelId,
      @Valid @RequestBody PuzzleProgressRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    var cmd =
        new SubmitProgressCommand(
            duelId, principal.id(), request.roundNumber(), request.moves(), request.timeMs());
    return toDtoFromView(submitInfiltrationUseCase.submitInfiltrationProgress(cmd));
  }

  @PostMapping("/{duelId}/join")
  @PreAuthorize("hasAnyRole('HERO','VILLAIN')")
  public DuelSessionResponse join(
      @PathVariable UUID duelId, @AuthenticationPrincipal UsuarioPrincipal principal) {
    return toDtoFromView(joinDuelUseCase.join(duelId, principal.id()));
  }

  @PostMapping("/{duelId}/progress")
  @PreAuthorize("isAuthenticated()")
  public DuelSessionResponse submitProgress(
      @PathVariable UUID duelId,
      @Valid @RequestBody PuzzleProgressRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    var cmd = new SubmitProgressCommand(
        duelId, principal.id(), request.roundNumber(), request.moves(), request.timeMs());
    return toDtoFromView(submitProgressUseCase.submit(cmd));
  }

  @PostMapping("/{duelId}/sabotage-choice")
  @PreAuthorize("hasRole('VILLAIN')")
  public ResponseEntity<Void> sabotageChoice(
      @PathVariable UUID duelId,
      @Valid @RequestBody SabotageChoiceRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    var session = duelPort.findById(duelId)
        .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    if (session.getStatus() != DuelStatus.VILLAIN_WON) {
      throw new br.edu.central.centrallj.application.exception.BadRequestException(
          "Sabotagem só pode ser escolhida após vitória do vilão.");
    }
    if (!session.getAttackerUserId().equals(principal.id())) {
      throw new br.edu.central.centrallj.application.exception.BadRequestException("Acesso negado.");
    }

    SabotageEvent event = new SabotageEvent();
    event.setId(UUID.randomUUID());
    event.setDuelSessionId(duelId);
    event.setMissionId(session.getMissionId());
    event.setAttackerUserId(principal.id());
    event.setSabotageType(request.sabotageType());
    event.setTargetScope(request.targetScope());
    event.setTargetUserId(request.targetUserId());
    event.setAppliedAt(Instant.now());
    event.setExpiresAt(Instant.now().plusSeconds(10 * 60L));
    sabotagePort.save(event);

    return ResponseEntity.noContent().build();
  }

  private DuelSessionResponse toDto(br.edu.central.centrallj.domain.DuelSession s) {
    return new DuelSessionResponse(
        s.getId(), s.getMissionId(), s.getAttackerUserId(), s.getDefenderUserId(),
        s.getSeed(), s.getPuzzleType(), s.getStatus(),
        s.getRoundCurrent(), s.getRoundMax(),
        s.getAttackerRoundsWon(), s.getDefenderRoundsWon(),
        s.getInfiltrationProgress(), 3,
        s.getStartedAt(), s.getFinishedAt(), s.getTimeoutAt());
  }

  private DuelSessionResponse toDtoFromView(DuelSessionView v) {
    return new DuelSessionResponse(
        v.id(), v.missionId(), v.attackerUserId(), v.defenderUserId(),
        v.seed(), v.puzzleType(), v.status(),
        v.roundCurrent(), v.roundMax(),
        v.attackerRoundsWon(), v.defenderRoundsWon(),
        v.infiltrationProgress(), v.infiltrationRequired(),
        v.startedAt(), v.finishedAt(), v.timeoutAt());
  }
}
