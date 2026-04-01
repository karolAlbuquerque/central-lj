package br.edu.central.centrallj.dto;

/** Corpo opcional para publicar um evento mínimo no Kafka (N1). */
public record EventPublishTestRequest(String mensagem) {}
