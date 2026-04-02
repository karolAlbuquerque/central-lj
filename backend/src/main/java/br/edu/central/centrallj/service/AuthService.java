package br.edu.central.centrallj.service;

import br.edu.central.centrallj.domain.Usuario;
import br.edu.central.centrallj.dto.AuthUserResponse;
import br.edu.central.centrallj.dto.LoginRequest;
import br.edu.central.centrallj.dto.LoginResponse;
import br.edu.central.centrallj.repository.UsuarioRepository;
import br.edu.central.centrallj.security.JwtService;
import br.edu.central.centrallj.security.UsuarioPrincipal;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    String email = request.email().trim().toLowerCase();
    Usuario usuario =
        usuarioRepository
            .findByEmailAndAtivoTrue(email)
            .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));
    if (!passwordEncoder.matches(request.password(), usuario.getSenhaHash())) {
      throw new BadCredentialsException("E-mail ou senha inválidos.");
    }
    String token = jwtService.createToken(usuario);
    return new LoginResponse(token, "Bearer", toResponse(usuario));
  }

  public AuthUserResponse toResponse(Usuario usuario) {
    UUID heroiId = usuario.getHeroi() != null ? usuario.getHeroi().getId() : null;
    return new AuthUserResponse(
        usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(), heroiId);
  }

  public AuthUserResponse toResponse(UsuarioPrincipal principal) {
    return new AuthUserResponse(
        principal.id(), principal.nome(), principal.email(), principal.role(), principal.heroiId());
  }
}
