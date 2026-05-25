package br.edu.central.centrallj.bootstrap;

import br.edu.central.centrallj.application.port.out.EquipePersistencePort;
import br.edu.central.centrallj.application.port.out.HeroiPersistencePort;
import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.EquipeHeroica;
import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.domain.HeroiDisponibilidade;
import br.edu.central.centrallj.domain.UserRole;
import br.edu.central.centrallj.domain.Usuario;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga mínima para banca/demo: 1 equipe, 1 herói, usuário ADMIN e usuário HERO vinculado. Idempotente
 * se já existir algum usuário.
 */
@Component
@Order(2000)
@ConditionalOnProperty(
    prefix = "central-lj.auth",
    name = "demo-seed",
    havingValue = "true",
    matchIfMissing = true)
public class DemoAuthDataLoader implements ApplicationRunner {

  /** IDs fixos documentados (docs/n2/09-autenticacao-e-papeis.md). */
  public static final UUID DEMO_EQUIPE_ID = UUID.fromString("22222222-2222-2222-2222-222222222201");

  public static final UUID DEMO_HEROI_ID = UUID.fromString("22222222-2222-2222-2222-222222222202");

  public static final UUID DEMO_ADMIN_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333301");
  public static final UUID DEMO_HERO_USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333302");

  private final UsuarioPersistencePort usuarioPersistencePort;
  private final EquipePersistencePort equipePersistencePort;
  private final HeroiPersistencePort heroiPersistencePort;
  private final PasswordEncoder passwordEncoder;

  public DemoAuthDataLoader(
      UsuarioPersistencePort usuarioPersistencePort,
      EquipePersistencePort equipePersistencePort,
      HeroiPersistencePort heroiPersistencePort,
      PasswordEncoder passwordEncoder) {
    this.usuarioPersistencePort = usuarioPersistencePort;
    this.equipePersistencePort = equipePersistencePort;
    this.heroiPersistencePort = heroiPersistencePort;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (usuarioPersistencePort.existsByEmail("coordenacao@central-lj.demo")) {
      return;
    }
    Instant now = Instant.now();

    EquipeHeroica eq = new EquipeHeroica();
    eq.setId(DEMO_EQUIPE_ID);
    eq.setNome("Equipe Demo Central");
    eq.setEspecialidadePrincipal("Resposta rápida");
    eq.setAtiva(true);
    eq.setCreatedAt(now);
    eq.setUpdatedAt(now);
    equipePersistencePort.save(eq);

    Heroi hero = new Heroi();
    hero.setId(DEMO_HEROI_ID);
    hero.setNomeHeroico("Guardião Demo");
    hero.setNomeCivil("Voluntário Integrador");
    hero.setEspecialidade("Patrulha urbana");
    hero.setStatusDisponibilidade(HeroiDisponibilidade.DISPONIVEL);
    hero.setNivel("A");
    hero.setAtivo(true);
    hero.setEquipe(eq);
    hero.setCreatedAt(now);
    hero.setUpdatedAt(now);
    heroiPersistencePort.save(hero);

    Usuario admin = new Usuario();
    admin.setId(DEMO_ADMIN_USER_ID);
    admin.setNome("Coordenação Demo");
    admin.setEmail("coordenacao@central-lj.demo");
    admin.setSenhaHash(passwordEncoder.encode("Admin@demo2026"));
    admin.setRole(UserRole.ADMIN);
    admin.setAtivo(true);
    admin.setHeroi(null);
    admin.setCreatedAt(now);
    admin.setUpdatedAt(now);
    usuarioPersistencePort.save(admin);

    Usuario heroUser = new Usuario();
    heroUser.setId(DEMO_HERO_USER_ID);
    heroUser.setNome("Guardião Demo (conta)");
    heroUser.setEmail("heroi.demo@central-lj.demo");
    heroUser.setSenhaHash(passwordEncoder.encode("Hero@demo2026"));
    heroUser.setRole(UserRole.HERO);
    heroUser.setAtivo(true);
    heroUser.setHeroi(hero);
    heroUser.setCreatedAt(now);
    heroUser.setUpdatedAt(now);
    usuarioPersistencePort.save(heroUser);
  }
}
