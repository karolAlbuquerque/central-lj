package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.AssignHeroRequest;
import br.edu.central.centrallj.adapter.in.web.dto.AssignTeamRequest;
import br.edu.central.centrallj.adapter.in.web.dto.CreateMissionRequest;
import br.edu.central.centrallj.adapter.in.web.dto.DashboardSummaryResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionDetailResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionHistoryEntryResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionResponse;
import br.edu.central.centrallj.adapter.in.web.mapper.WebDtoMapper;
import br.edu.central.centrallj.adapter.in.web.security.MissionViewPolicy;
import br.edu.central.centrallj.adapter.in.web.security.UsuarioPrincipal;
import br.edu.central.centrallj.adapter.out.realtime.SseMissionNotificationAdapter;
import br.edu.central.centrallj.application.exception.BadRequestException;
import br.edu.central.centrallj.application.port.in.AssignMissionUseCase;
import br.edu.central.centrallj.application.port.in.CreateMissionUseCase;
import br.edu.central.centrallj.application.port.in.GetMissionUseCase;
import br.edu.central.centrallj.domain.MissionStatus;
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

  private final CreateMissionUseCase createMissionUseCase;
  private final GetMissionUseCase getMissionUseCase;
  private final AssignMissionUseCase assignMissionUseCase;
  private final SseMissionNotificationAdapter sseNotificationAdapter;
  private final MissionViewPolicy missionViewPolicy;
  private final WebDtoMapper webDtoMapper;

  public MissionController(
      CreateMissionUseCase createMissionUseCase,
      GetMissionUseCase getMissionUseCase,
      AssignMissionUseCase assignMissionUseCase,
      SseMissionNotificationAdapter sseNotificationAdapter,
      MissionViewPolicy missionViewPolicy,
      WebDtoMapper webDtoMapper) {
    this.createMissionUseCase = createMissionUseCase;
    this.getMissionUseCase = getMissionUseCase;
    this.assignMissionUseCase = assignMissionUseCase;
    this.sseNotificationAdapter = sseNotificationAdapter;
    this.missionViewPolicy = missionViewPolicy;
    this.webDtoMapper = webDtoMapper;
  }

  @GetMapping("/dashboard/summary")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public DashboardSummaryResponse dashboardSummary() {
    return webDtoMapper.toDto(getMissionUseCase.dashboardSummary());
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @PreAuthorize("isAuthenticated()")
  public SseEmitter missionEventStream() {
    return sseNotificationAdapter.subscribe(Duration.ofMinutes(30));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public ResponseEntity<MissionResponse> create(@Valid @RequestBody CreateMissionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(webDtoMapper.toDto(createMissionUseCase.create(webDtoMapper.toCommand(request))));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<MissionResponse> list() {
    return getMissionUseCase.listAll().stream().map(webDtoMapper::toDto).toList();
  }

  @GetMapping("/recent")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<MissionResponse> recent() {
    return getMissionUseCase.recentMissions().stream().map(webDtoMapper::toDto).toList();
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public List<MissionResponse> listByStatus(@PathVariable String status) {
    try {
      MissionStatus st = MissionStatus.valueOf(status.toUpperCase(Locale.ROOT));
      return getMissionUseCase.listByStatus(st).stream().map(webDtoMapper::toDto).toList();
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Status inválido: " + status);
    }
  }

  @GetMapping("/{id}/history")
  @PreAuthorize("isAuthenticated()")
  public List<MissionHistoryEntryResponse> getHistory(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    var detail = getMissionUseCase.getDetail(id);
    missionViewPolicy.assertCanView(principal, detail.missao());
    return getMissionUseCase.getHistory(id).stream().map(webDtoMapper::toDto).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public MissionDetailResponse getDetail(
      @PathVariable UUID id, @AuthenticationPrincipal UsuarioPrincipal principal) {
    var detail = getMissionUseCase.getDetail(id);
    missionViewPolicy.assertCanView(principal, detail.missao());
    return webDtoMapper.toDto(detail);
  }

  @PatchMapping("/{id}/assign-hero")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public MissionResponse assignHero(
      @PathVariable UUID id, @Valid @RequestBody AssignHeroRequest request) {
    return webDtoMapper.toDto(
        assignMissionUseCase.assignHero(id, webDtoMapper.toCommand(request)));
  }

  @PatchMapping("/{id}/assign-team")
  @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
  public MissionResponse assignTeam(
      @PathVariable UUID id, @Valid @RequestBody AssignTeamRequest request) {
    return webDtoMapper.toDto(
        assignMissionUseCase.assignTeam(id, webDtoMapper.toCommand(request)));
  }
}
