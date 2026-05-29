package br.edu.central.centrallj.application.port.in;

import br.edu.central.centrallj.application.model.LoginResult;
import br.edu.central.centrallj.application.model.RegisterCommand;

public interface RegisterUseCase {
  LoginResult register(RegisterCommand command);
}
