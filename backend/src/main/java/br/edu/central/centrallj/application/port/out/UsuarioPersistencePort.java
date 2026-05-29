package br.edu.central.centrallj.application.port.out;

import br.edu.central.centrallj.domain.Usuario;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioPersistencePort {
  Optional<Usuario> findByEmailAndAtivoTrue(String email);
  Optional<Usuario> findById(UUID id);
  boolean existsByEmail(String email);
  Usuario save(Usuario usuario);
}
