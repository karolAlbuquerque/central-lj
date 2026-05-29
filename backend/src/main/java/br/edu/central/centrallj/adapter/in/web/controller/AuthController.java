package br.edu.central.centrallj.adapter.in.web.controller;

import br.edu.central.centrallj.adapter.in.web.dto.AuthUserResponse;
import br.edu.central.centrallj.adapter.in.web.dto.LoginRequest;
import br.edu.central.centrallj.adapter.in.web.dto.LoginResponse;
import br.edu.central.centrallj.adapter.in.web.dto.RegisterRequest;
import br.edu.central.centrallj.adapter.in.web.mapper.WebDtoMapper;
import br.edu.central.centrallj.adapter.in.web.security.UsuarioPrincipal;
import br.edu.central.centrallj.application.port.in.AuthenticateUseCase;
import br.edu.central.centrallj.application.port.in.RegisterUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticateUseCase authenticateUseCase;
  private final RegisterUseCase registerUseCase;
  private final WebDtoMapper webDtoMapper;

  public AuthController(AuthenticateUseCase authenticateUseCase, RegisterUseCase registerUseCase, WebDtoMapper webDtoMapper) {
    this.authenticateUseCase = authenticateUseCase;
    this.registerUseCase = registerUseCase;
    this.webDtoMapper = webDtoMapper;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return webDtoMapper.toDto(authenticateUseCase.login(webDtoMapper.toCommand(request)));
  }

  @GetMapping("/me")
  public ResponseEntity<AuthUserResponse> me(@AuthenticationPrincipal UsuarioPrincipal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(
        webDtoMapper.toDto(
            authenticateUseCase.fromPrincipal(
                principal.id(),
                principal.nome(),
                principal.email(),
                principal.role(),
                principal.heroiId())));
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
    return webDtoMapper.toDto(registerUseCase.register(webDtoMapper.toRegisterCommand(request)));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
