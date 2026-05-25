package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.Usuario;
import java.util.Optional;

public interface UsuarioPersistencePort {
  Optional<Usuario> findByEmailAndAtivoTrue(String email);
  boolean existsByEmail(String email);
  Usuario save(Usuario usuario);
}
