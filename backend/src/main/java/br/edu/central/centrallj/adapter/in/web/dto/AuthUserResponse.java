package br.edu.central.centrallj.adapter.in.web.dto;

import br.edu.central.centrallj.domain.UserRole;
import java.util.UUID;

public record AuthUserResponse(
    UUID id, String nome, String email, UserRole role, UUID heroiId) {}
