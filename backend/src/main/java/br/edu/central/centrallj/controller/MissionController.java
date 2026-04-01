package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.dto.CreateMissionRequest;
import br.edu.central.centrallj.dto.DashboardSummaryResponse;
import br.edu.central.centrallj.dto.MissionDetailResponse;
import br.edu.central.centrallj.dto.MissionResponse;
import br.edu.central.centrallj.exception.BadRequestException;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
  private final MissionRealtimeNotifier missionRealtimeNotifier;

  public MissionController(
      MissionCommandService missionCommandService,
      MissionQueryService missionQueryService,
      MissionRealtimeNotifier missionRealtimeNotifier) {
    this.missionCommandService = missionCommandService;
    this.missionQueryService = missionQueryService;
    this.missionRealtimeNotifier = missionRealtimeNotifier;
  }

  /** Resumo para o painel administrativo (métricas + missões recentes). */
  @GetMapping("/dashboard/summary")
  public DashboardSummaryResponse dashboardSummary() {
    return missionQueryService.dashboardSummary();
  }

  /** Stream SSE: notificações quando uma missão é atualizada (complementa polling no front). */
  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter missionEventStream() {
    return missionRealtimeNotifier.subscribe(Duration.ofMinutes(30));
  }

  @PostMapping
  public ResponseEntity<MissionResponse> create(@Valid @RequestBody CreateMissionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(missionCommandService.create(request));
  }

  @GetMapping
  public List<MissionResponse> list() {
    return missionQueryService.listAll();
  }

  @GetMapping("/recent")
  public List<MissionResponse> recent() {
    return missionQueryService.recentMissions();
  }

  @GetMapping("/status/{status}")
  public List<MissionResponse> listByStatus(@PathVariable String status) {
    try {
      MissionStatus st = MissionStatus.valueOf(status.toUpperCase(Locale.ROOT));
      return missionQueryService.listByStatus(st);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Status inválido: " + status);
    }
  }

  /** Detalhe com timeline (histórico de status). */
  @GetMapping("/{id}")
  public MissionDetailResponse getDetail(@PathVariable UUID id) {
    return missionQueryService.getDetail(id);
  }
}
