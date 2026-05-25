package br.edu.central.centrallj.adapter.in.web.dto;

public record LoginResponse(String accessToken, String tokenType, AuthUserResponse user) {}
