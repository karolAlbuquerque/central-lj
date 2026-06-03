package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.adapter.in.web.security.JwtService;
import br.edu.central.centrallj.application.model.AuthUserView;
import br.edu.central.centrallj.application.model.LoginCommand;
import br.edu.central.centrallj.application.model.LoginResult;
import br.edu.central.centrallj.application.model.RegisterCommand;
import br.edu.central.centrallj.application.port.in.AuthenticateUseCase;
import br.edu.central.centrallj.application.port.in.RegisterUseCase;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import br.edu.central.centrallj.domain.UserRole;
import br.edu.central.centrallj.domain.Usuario;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService implements AuthenticateUseCase, RegisterUseCase {

  private final UsuarioPersistencePort usuarioPersistencePort;
  private final HeroiPersistencePort heroiPersistencePort;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UsuarioPersistencePort usuarioPersistencePort,
      HeroiPersistencePort heroiPersistencePort,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.usuarioPersistencePort = usuarioPersistencePort;
    this.heroiPersistencePort = heroiPersistencePort;
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

  @Override
  @Transactional
  public LoginResult register(RegisterCommand command) {
    String email = command.email().trim().toLowerCase();
    if (usuarioPersistencePort.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
    }
    Instant now = Instant.now();
    Usuario usuario = new Usuario();
    usuario.setId(UUID.randomUUID());
    usuario.setNome(command.nome().trim());
    usuario.setEmail(email);
    usuario.setSenhaHash(passwordEncoder.encode(command.password()));
    usuario.setRole(command.role());
    usuario.setAtivo(true);
    usuario.setCreatedAt(now);
    usuario.setUpdatedAt(now);
    if (command.role() == UserRole.HERO) {
      usuario.setHeroi(createHeroForNewAccount(command.nome().trim(), now));
    }
    Usuario saved = usuarioPersistencePort.save(usuario);
    String token = jwtService.createToken(saved);
    return new LoginResult(token, "Bearer", toView(saved));
  }

  private Heroi createHeroForNewAccount(String nome, Instant now) {
    Heroi heroi = new Heroi();
    heroi.setId(UUID.randomUUID());
    heroi.setNomeHeroico(nome);
    heroi.setNomeCivil(nome);
    heroi.setEspecialidade("Operações de campo");
    heroi.setStatusDisponibilidade(HeroiDisponibilidade.DISPONIVEL);
    heroi.setNivel("C");
    heroi.setAtivo(true);
    heroi.setEquipe(null);
    heroi.setCreatedAt(now);
    heroi.setUpdatedAt(now);
    return heroiPersistencePort.save(heroi);
  }

  public AuthUserView toView(Usuario usuario) {
    UUID heroiId = usuario.getHeroi() != null ? usuario.getHeroi().getId() : null;
    return new AuthUserView(
        usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(), heroiId);
  }
}
