package br.edu.central.centrallj.adapter.in.web.dto;

/** Corpo opcional para publicar um evento mínimo no Kafka (N1). */
public record EventPublishTestRequest(String mensagem) {}
