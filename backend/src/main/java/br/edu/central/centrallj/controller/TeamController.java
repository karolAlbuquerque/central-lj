package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.application.port.in.teams.TeamUseCase;
import br.edu.central.centrallj.dto.CreateEquipeRequest;
import br.edu.central.centrallj.dto.EquipeDetailResponse;
import br.edu.central.centrallj.dto.EquipeResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@Validated
@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
public class TeamController {

  private final TeamUseCase teamUseCase;

  public TeamController(TeamUseCase teamUseCase) {
    this.teamUseCase = teamUseCase;
  }

  @PostMapping
  public ResponseEntity<EquipeResponse> create(@Valid @RequestBody CreateEquipeRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(teamUseCase.create(request));
  }

  @GetMapping
  public List<EquipeResponse> list() {
    return teamUseCase.listAll();
  }

  @GetMapping("/{id}")
  public EquipeDetailResponse getDetail(@PathVariable UUID id) {
    return teamUseCase.getDetail(id);
  }
}
