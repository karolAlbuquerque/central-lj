package br.edu.central.centrallj.security;

import br.edu.central.centrallj.domain.UserRole;
import br.edu.central.centrallj.dto.MissionResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class MissionViewPolicy {

  /**
   * ADMIN/OPERATOR: acesso total. HERO: apenas missão atualmente designada a ele. Sem principal
   * (perfil teste com segurança desligada): libera.
   */
  public void assertCanView(UsuarioPrincipal principal, MissionResponse missao) {
    if (principal == null) {
      return;
    }
    if (principal.role() == UserRole.ADMIN || principal.role() == UserRole.OPERATOR) {
      return;
    }
    if (principal.role() == UserRole.HERO && principal.heroiId() != null) {
      var atr = missao.atribuicao();
      boolean ok = atr != null && principal.heroiId().equals(atr.heroiId());
      if (ok) {
        return;
      }
    }
    throw new AccessDeniedException("Você não tem acesso a esta missão.");
  }
}
