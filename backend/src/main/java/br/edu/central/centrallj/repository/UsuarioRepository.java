package br.edu.central.centrallj.repository;

import br.edu.central.centrallj.domain.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

  @EntityGraph(attributePaths = {"heroi"})
  Optional<Usuario> findByEmailAndAtivoTrue(String email);

  boolean existsByEmail(String email);
}
