package br.edu.central.centrallj.adapter.out.persistence.repository;

import br.edu.central.centrallj.adapter.out.persistence.entity.UsuarioEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

  @EntityGraph(attributePaths = {"heroi"})
  Optional<UsuarioEntity> findByEmailAndAtivoTrue(String email);

  boolean existsByEmail(String email);
}
