package br.edu.central.centrallj.application.port.in.auth;

import br.edu.central.centrallj.dto.LoginRequest;
import br.edu.central.centrallj.dto.LoginResponse;

/** Porta de entrada: autenticar usuário e emitir token. */
public interface LoginUseCase {
  LoginResponse login(LoginRequest request);
}

