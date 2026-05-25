package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.mapper.PersistenceEntityMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.UsuarioRepository;
import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.Usuario;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UsuarioJpaPersistenceAdapter implements UsuarioPersistencePort {

  private final UsuarioRepository jpa;
  private final PersistenceEntityMapper mapper;

  public UsuarioJpaPersistenceAdapter(UsuarioRepository jpa, PersistenceEntityMapper mapper) {
    this.jpa = jpa;
    this.mapper = mapper;
  }

  @Override
  public Optional<Usuario> findByEmailAndAtivoTrue(String email) {
    return jpa.findByEmailAndAtivoTrue(email).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpa.existsByEmail(email);
  }

  @Override
  public Usuario save(Usuario usuario) {
    return mapper.toDomain(jpa.save(mapper.toEntity(usuario)));
  }
}
