package br.edu.central.centrallj.domain;

public enum DuelStatus {
  /** Vilão resolvendo os 3 puzzles de infiltração antes do alerta à missão. */
  INFILTRATING,
  PENDING,
  ACTIVE,
  HERO_WON,
  VILLAIN_WON,
  CANCELLED,
  TIMEOUT
}
