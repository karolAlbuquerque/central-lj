package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.dto.MissionTestRequest;
import br.edu.central.centrallj.service.MissionTestService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/missions")
public class MissionTestController {

  private final MissionTestService missionTestService;

  public MissionTestController(MissionTestService missionTestService) {
    this.missionTestService = missionTestService;
  }

  @PostMapping("/test")
  public ResponseEntity<Map<String, Object>> testReceive(@Valid @RequestBody MissionTestRequest request) {
    return ResponseEntity.ok(missionTestService.acceptTestMission(request));
  }
}
