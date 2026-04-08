package br.edu.central.centrallj.application.port.in.auth;

import br.edu.central.centrallj.dto.AuthUserResponse;
import br.edu.central.centrallj.security.UsuarioPrincipal;

/** Porta de entrada: expor o usuário autenticado (me). */
public interface GetCurrentUserUseCase {
  AuthUserResponse toResponse(UsuarioPrincipal principal);
}

