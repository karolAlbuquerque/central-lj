package br.edu.central.centrallj.domain;

public enum MissionCombatState {
  LOBBY,
  ACTIVE,
  NORMAL,
  ALERTA_INFILTRACAO,
  EM_DUELO,
  SABOTADA,
  /** Missão encerrada — herói perdeu o duelo para o vilão. */
  DERROTADA,
  DEFENDIDA,
  SEM_CHEFE,
  EM_CRISE,
  COMPROMETIDA
}
