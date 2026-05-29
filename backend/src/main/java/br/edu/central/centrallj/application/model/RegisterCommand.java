package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.UserRole;

public record RegisterCommand(String nome, String email, String password, UserRole role) {}
