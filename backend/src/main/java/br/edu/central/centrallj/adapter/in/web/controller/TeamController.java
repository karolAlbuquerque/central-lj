package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.CreateEquipeRequest;
import br.edu.central.centrallj.adapter.in.web.dto.EquipeDetailResponse;
import br.edu.central.centrallj.adapter.in.web.dto.EquipeResponse;
import br.edu.central.centrallj.adapter.in.web.mapper.WebDtoMapper;
import br.edu.central.centrallj.application.port.in.ManageEquipeUseCase;
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

  private final ManageEquipeUseCase manageEquipeUseCase;
  private final WebDtoMapper webDtoMapper;

  public TeamController(ManageEquipeUseCase manageEquipeUseCase, WebDtoMapper webDtoMapper) {
    this.manageEquipeUseCase = manageEquipeUseCase;
    this.webDtoMapper = webDtoMapper;
  }

  @PostMapping
  public ResponseEntity<EquipeResponse> create(@Valid @RequestBody CreateEquipeRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(webDtoMapper.toDto(manageEquipeUseCase.create(webDtoMapper.toCommand(request))));
  }

  @GetMapping
  public List<EquipeResponse> list() {
    return manageEquipeUseCase.listAll().stream().map(webDtoMapper::toDto).toList();
  }

  @GetMapping("/{id}")
  public EquipeDetailResponse getDetail(@PathVariable UUID id) {
    return webDtoMapper.toDto(manageEquipeUseCase.getDetail(id));
  }
}
