package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.CreateMissionTaskRequest;
import br.edu.central.centrallj.adapter.in.web.dto.CreatePlayerMissionRequest;
import br.edu.central.centrallj.adapter.in.web.dto.ExecuteTaskRequest;
import br.edu.central.centrallj.adapter.in.web.dto.InviteMemberRequest;
import br.edu.central.centrallj.adapter.in.web.dto.MissionMemberResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionTaskResponse;
import br.edu.central.centrallj.adapter.in.web.dto.PlayerMissionDetailResponse;
import br.edu.central.centrallj.adapter.in.web.dto.PlayerMissionResponse;
import br.edu.central.centrallj.adapter.in.web.dto.StartMissionRequest;
import br.edu.central.centrallj.adapter.in.web.security.UsuarioPrincipal;
import br.edu.central.centrallj.application.model.AssignTaskCommand;
import br.edu.central.centrallj.application.model.CreatePlayerMissionCommand;
import br.edu.central.centrallj.application.model.ExecuteTaskCommand;
import br.edu.central.centrallj.application.model.MissionMemberView;
import br.edu.central.centrallj.application.model.MissionTaskView;
import br.edu.central.centrallj.application.model.PlayerMissionDetailView;
import br.edu.central.centrallj.application.model.PlayerMissionView;
import br.edu.central.centrallj.application.port.in.AssignMissionTaskUseCase;
import br.edu.central.centrallj.application.port.in.CloseMissionUseCase;
import br.edu.central.centrallj.application.port.in.CreatePlayerMissionUseCase;
import br.edu.central.centrallj.application.port.in.ExecuteMissionTaskUseCase;
import br.edu.central.centrallj.application.port.in.GetPlayerMissionUseCase;
import br.edu.central.centrallj.application.port.in.InviteMissionMemberUseCase;
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
@RequestMapping("/api/player-missions")
@Validated
public class PlayerMissionController {

  private final CreatePlayerMissionUseCase createUseCase;
  private final GetPlayerMissionUseCase getUseCase;
  private final InviteMissionMemberUseCase inviteUseCase;
  private final AssignMissionTaskUseCase taskUseCase;
  private final ExecuteMissionTaskUseCase executeTaskUseCase;
  private final CloseMissionUseCase closeUseCase;

  public PlayerMissionController(
      CreatePlayerMissionUseCase createUseCase,
      GetPlayerMissionUseCase getUseCase,
      InviteMissionMemberUseCase inviteUseCase,
      AssignMissionTaskUseCase taskUseCase,
      ExecuteMissionTaskUseCase executeTaskUseCase,
      CloseMissionUseCase closeUseCase) {
    this.createUseCase = createUseCase;
    this.getUseCase = getUseCase;
    this.inviteUseCase = inviteUseCase;
    this.taskUseCase = taskUseCase;
    this.executeTaskUseCase = executeTaskUseCase;
    this.closeUseCase = closeUseCase;
  }

  @PostMapping
  @PreAuthorize("hasRole('HERO')")
  public ResponseEntity<PlayerMissionResponse> create(
      @Valid @RequestBody CreatePlayerMissionRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    var view = createUseCase.create(
        new CreatePlayerMissionCommand(
            request.titulo(),
            request.descricao(),
            request.minPlayers(),
            principal.id()));
    return ResponseEntity.status(HttpStatus.CREATED).body(toDto(view));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('HERO','ADMIN')")
  public List<PlayerMissionResponse> list(@AuthenticationPrincipal UsuarioPrincipal principal) {
    boolean isAdmin = principal.role().name().equals("ADMIN");
    List<PlayerMissionView> views =
        isAdmin ? getUseCase.listAll() : getUseCase.listForMember(principal.id());
    return views.stream().map(this::toDto).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public PlayerMissionDetailResponse getDetail(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    return toDetailDto(getUseCase.getDetail(id, principal.id()));
  }

  @PostMapping("/{id}/members/invite")
  @PreAuthorize("hasRole('HERO')")
  public ResponseEntity<MissionMemberResponse> invite(
      @PathVariable UUID id,
      @Valid @RequestBody InviteMemberRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(toMemberDto(inviteUseCase.invite(id, request.userId(), principal.id())));
  }

  @PostMapping("/{id}/members/accept")
  @PreAuthorize("hasRole('HERO')")
  public MissionMemberResponse acceptInvite(
      @PathVariable UUID id,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    return toMemberDto(inviteUseCase.acceptInvite(id, principal.id()));
  }

  @PostMapping("/{id}/members/decline")
  @PreAuthorize("hasRole('HERO')")
  public MissionMemberResponse declineInvite(
      @PathVariable UUID id,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    return toMemberDto(inviteUseCase.declineInvite(id, principal.id()));
  }

  @PostMapping("/{id}/start")
  @PreAuthorize("hasRole('HERO')")
  public PlayerMissionResponse startMission(
      @PathVariable UUID id,
      @RequestBody(required = false) StartMissionRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    boolean force = request != null && Boolean.TRUE.equals(request.forceStart());
    return toDto(inviteUseCase.startMission(id, principal.id(), force));
  }

  @GetMapping("/{id}/members")
  @PreAuthorize("isAuthenticated()")
  public List<MissionMemberResponse> listMembers(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    return getUseCase.getDetail(id, principal.id()).members().stream().map(this::toMemberDto).toList();
  }

  @PostMapping("/{id}/tasks")
  @PreAuthorize("hasRole('HERO')")
  public ResponseEntity<MissionTaskResponse> createTask(
      @PathVariable UUID id,
      @Valid @RequestBody CreateMissionTaskRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    var cmd = new AssignTaskCommand(
        null, id, request.assignedToUserId(),
        request.title(), request.description(),
        request.critical(), request.puzzleType(), request.dependsOnTaskId(), principal.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(toTaskDto(taskUseCase.createTask(cmd)));
  }

  @PostMapping("/{id}/tasks/{taskId}/execute")
  @PreAuthorize("hasRole('HERO')")
  public MissionTaskResponse executeTask(
      @PathVariable UUID id,
      @PathVariable UUID taskId,
      @Valid @RequestBody ExecuteTaskRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    var cmd =
        new ExecuteTaskCommand(
            id, taskId, principal.id(), request.action(), request.moves());
    return toTaskDto(executeTaskUseCase.execute(cmd));
  }

  @PatchMapping("/{id}/tasks/{taskId}")
  @PreAuthorize("hasRole('HERO')")
  public MissionTaskResponse updateTask(
      @PathVariable UUID id,
      @PathVariable UUID taskId,
      @Valid @RequestBody CreateMissionTaskRequest request,
      @AuthenticationPrincipal UsuarioPrincipal principal) {
    var cmd = new AssignTaskCommand(
        taskId, id, request.assignedToUserId(),
        request.title(), request.description(),
        request.critical(), request.puzzleType(), request.dependsOnTaskId(), principal.id());
    return toTaskDto(taskUseCase.updateTask(cmd));
  }

  @PostMapping("/{id}/close")
  @PreAuthorize("hasRole('HERO')")
  public ResponseEntity<Void> close(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    closeUseCase.close(id, principal.id());
    return ResponseEntity.noContent().build();
  }

  private PlayerMissionResponse toDto(PlayerMissionView v) {
    return new PlayerMissionResponse(
        v.id(), v.titulo(), v.descricao(), v.ownerUserId(),
        v.combatState(), v.minPlayers(), v.partySeed(), v.startedByUserId(),
        v.startedAt(), v.createdAt(), v.updatedAt());
  }

  private PlayerMissionDetailResponse toDetailDto(PlayerMissionDetailView v) {
    return new PlayerMissionDetailResponse(
        toDto(v.mission()),
        v.members().stream().map(this::toMemberDto).toList(),
        v.tasks().stream().map(this::toTaskDto).toList());
  }

  private MissionMemberResponse toMemberDto(MissionMemberView v) {
    return new MissionMemberResponse(
        v.id(), v.missionId(), v.userId(), v.userName(), v.role(), v.inviteStatus(), v.joinedAt());
  }

  private MissionTaskResponse toTaskDto(MissionTaskView v) {
    return new MissionTaskResponse(
        v.id(), v.missionId(), v.assignedToUserId(), v.title(), v.description(),
        v.status(), v.critical(), v.dependsOnTaskId(), v.createdAt(), v.updatedAt(),
        v.canExecute(), v.puzzleSeed(), v.puzzleType());
  }
}
