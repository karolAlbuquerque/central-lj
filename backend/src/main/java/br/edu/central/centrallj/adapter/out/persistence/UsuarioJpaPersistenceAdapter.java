package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.Usuario;
import br.edu.central.centrallj.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UsuarioJpaPersistenceAdapter implements UsuarioPersistencePort {

  private final UsuarioRepository jpa;

  public UsuarioJpaPersistenceAdapter(UsuarioRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<Usuario> findByEmailAndAtivoTrue(String email) {
    return jpa.findByEmailAndAtivoTrue(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpa.existsByEmail(email);
  }

  @Override
  public Usuario save(Usuario usuario) {
    return jpa.save(usuario);
  }
}
