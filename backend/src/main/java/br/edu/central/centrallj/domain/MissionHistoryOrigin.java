package br.edu.central.centrallj.domain;

public enum MissionHistoryOrigin {
  /** Registro inicial via API REST. */
  API_REGISTRO,
  /** Atribuição manual de herói ou equipe à missão. */
  API_ATRIBUICAO,
  /** Pipeline assíncrono após evento Kafka. */
  KAFKA_WORKFLOW,
  /** Falha tratada no workflow/interrupção. */
  KAFKA_WORKFLOW_ERRO
}
