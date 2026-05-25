package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.UserRole;
import java.util.UUID;

public record AuthUserView(UUID id, String nome, String email, UserRole role, UUID heroiId) {}
