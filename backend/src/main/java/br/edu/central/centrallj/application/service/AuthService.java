package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.adapter.in.web.security.JwtService;
import br.edu.central.centrallj.application.model.AuthUserView;
import br.edu.central.centrallj.application.model.LoginCommand;
import br.edu.central.centrallj.application.model.LoginResult;
import br.edu.central.centrallj.application.port.in.AuthenticateUseCase;
import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.UserRole;
import br.edu.central.centrallj.domain.Usuario;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements AuthenticateUseCase {

  private final UsuarioPersistencePort usuarioPersistencePort;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UsuarioPersistencePort usuarioPersistencePort,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.usuarioPersistencePort = usuarioPersistencePort;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Override
  @Transactional(readOnly = true)
  public LoginResult login(LoginCommand command) {
    String email = command.email().trim().toLowerCase();
    Usuario usuario =
        usuarioPersistencePort
            .findByEmailAndAtivoTrue(email)
            .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos."));
    if (!passwordEncoder.matches(command.password(), usuario.getSenhaHash())) {
      throw new BadCredentialsException("E-mail ou senha inválidos.");
    }
    String token = jwtService.createToken(usuario);
    return new LoginResult(token, "Bearer", toView(usuario));
  }

  @Override
  public AuthUserView fromPrincipal(UUID id, String nome, String email, UserRole role, UUID heroiId) {
    return new AuthUserView(id, nome, email, role, heroiId);
  }

  public AuthUserView toView(Usuario usuario) {
    UUID heroiId = usuario.getHeroi() != null ? usuario.getHeroi().getId() : null;
    return new AuthUserView(
        usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(), heroiId);
  }
}
