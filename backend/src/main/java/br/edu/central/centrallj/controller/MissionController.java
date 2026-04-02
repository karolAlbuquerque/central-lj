package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.dto.AssignHeroRequest;
import br.edu.central.centrallj.dto.AssignTeamRequest;
import br.edu.central.centrallj.dto.CreateMissionRequest;
import br.edu.central.centrallj.dto.DashboardSummaryResponse;
import br.edu.central.centrallj.dto.MissionDetailResponse;
import br.edu.central.centrallj.dto.MissionHistoryEntryResponse;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.exception.BadRequestException;
import br.edu.central.centrallj.security.MissionViewPolicy;
import br.edu.central.centrallj.security.UsuarioPrincipal;
import br.edu.central.centrallj.service.MissionAssignmentService;
import br.edu.central.centrallj.service.MissionCommandService;
import br.edu.central.centrallj.service.MissionQueryService;
import br.edu.central.centrallj.service.MissionRealtimeNotifier;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/missions")
@Validated
public class MissionController {

  private final MissionCommandService missionCommandService;
  private final MissionQueryService missionQueryService;
  private final MissionAssignmentService missionAssignmentService;
  private final MissionRealtimeNotifier missionRealtimeNotifier;
  private final MissionViewPolicy missionViewPolicy;

  public MissionController(
      MissionCommandService missionCommandService,
      MissionQueryService missionQueryService,
      MissionAssignmentService missionAssignmentService,
      MissionRealtimeNotifier missionRealtimeNotifier,
      MissionViewPolicy missionViewPolicy) {
    this.missionCommandService = missionCommandService;
    this.missionQueryService = missionQueryService;
    this.missionAssignmentService = missionAssignmentService;
    this.missionRealtimeNotifier = missionRealtimeNotifier;
    this.missionViewPolicy = missionViewPolicy;
  }

  @GetMapping("/dashboard/summary")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public DashboardSummaryResponse dashboardSummary() {
    return missionQueryService.dashboardSummary();
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @PreAuthorize("isAuthenticated()")
  public SseEmitter missionEventStream() {
    return missionRealtimeNotifier.subscribe(Duration.ofMinutes(30));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public ResponseEntity<MissionResponse> create(@Valid @RequestBody CreateMissionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(missionCommandService.create(request));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<MissionResponse> list() {
    return missionQueryService.listAll();
  }

  @GetMapping("/recent")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<MissionResponse> recent() {
    return missionQueryService.recentMissions();
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<MissionResponse> listByStatus(@PathVariable String status) {
    try {
      MissionStatus st = MissionStatus.valueOf(status.toUpperCase(Locale.ROOT));
      return missionQueryService.listByStatus(st);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Status inválido: " + status);
    }
  }

  @GetMapping("/{id}/history")
  @PreAuthorize("isAuthenticated()")
  public List<MissionHistoryEntryResponse> getHistory(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    MissionDetailResponse detail = missionQueryService.getDetail(id);
    missionViewPolicy.assertCanView(principal, detail.missao());
    return missionQueryService.getHistory(id);
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public MissionDetailResponse getDetail(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    MissionDetailResponse detail = missionQueryService.getDetail(id);
    missionViewPolicy.assertCanView(principal, detail.missao());
    return detail;
  }

  @PatchMapping("/{id}/assign-hero")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public MissionResponse assignHero(
      @PathVariable UUID id, @Valid @RequestBody AssignHeroRequest request) {
    return missionAssignmentService.assignHero(id, request);
  }

  @PatchMapping("/{id}/assign-team")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public MissionResponse assignTeam(
      @PathVariable UUID id, @Valid @RequestBody AssignTeamRequest request) {
    return missionAssignmentService.assignTeam(id, request);
  }
}
