package br.edu.central.centrallj.dto;

public record LoginResponse(String accessToken, String tokenType, AuthUserResponse user) {}
