package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.application.port.in.auth.GetCurrentUserUseCase;
import br.edu.central.centrallj.application.port.in.auth.LoginUseCase;
import br.edu.central.centrallj.dto.AuthUserResponse;
import br.edu.central.centrallj.dto.LoginRequest;
import br.edu.central.centrallj.dto.LoginResponse;
import br.edu.central.centrallj.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final GetCurrentUserUseCase currentUserUseCase;

  public AuthController(LoginUseCase loginUseCase, GetCurrentUserUseCase currentUserUseCase) {
    this.loginUseCase = loginUseCase;
    this.currentUserUseCase = currentUserUseCase;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return loginUseCase.login(request);
  }

  @GetMapping("/me")
  public ResponseEntity<AuthUserResponse> me(@AuthenticationPrincipal UsuarioPrincipal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(currentUserUseCase.toResponse(principal));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
