package br.edu.central.centrallj.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Nome é obrigatório.")
        @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres.")
        String nome,
    @NotBlank(message = "E-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,
    @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres.")
        String password,
    @NotBlank(message = "Papel é obrigatório.")
        @Pattern(regexp = "HERO|VILLAIN", message = "Papel inválido. Use HERO ou VILLAIN.")
        String role
) {}
