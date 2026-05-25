package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.AuthUserView;
import br.edu.central.centrallj.application.model.LoginCommand;
import br.edu.central.centrallj.application.model.LoginResult;
import br.edu.central.centrallj.domain.UserRole;
import java.util.UUID;

public interface AuthenticateUseCase {
  LoginResult login(LoginCommand command);

  AuthUserView fromPrincipal(UUID id, String nome, String email, UserRole role, UUID heroiId);
}
