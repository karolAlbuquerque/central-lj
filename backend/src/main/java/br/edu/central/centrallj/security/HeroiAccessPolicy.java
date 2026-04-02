package br.edu.central.centrallj.security;

import br.edu.central.centrallj.domain.UserRole;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class HeroiAccessPolicy {

  public void assertManageOrSelf(UsuarioPrincipal principal, UUID heroiId) {
    if (principal == null) {
      return;
    }
    if (principal.role() == UserRole.ADMIN || principal.role() == UserRole.OPERATOR) {
      return;
    }
    if (principal.role() == UserRole.HERO
        && principal.heroiId() != null
        && principal.heroiId().equals(heroiId)) {
      return;
    }
    throw new AccessDeniedException("Acesso negado a este herói.");
  }
}
