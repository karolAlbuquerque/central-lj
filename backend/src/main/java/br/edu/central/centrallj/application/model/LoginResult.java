package br.edu.central.centrallj.application.model;

public record LoginResult(String accessToken, String tokenType, AuthUserView user) {}
