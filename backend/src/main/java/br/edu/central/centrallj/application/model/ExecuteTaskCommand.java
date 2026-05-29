package br.edu.central.centrallj.application.model;

import java.util.List;
import java.util.UUID;

public record ExecuteTaskCommand(
    UUID missionId,
    UUID taskId,
    UUID requestingUserId,
    TaskAction action,
    List<Integer> moves) {

  public enum TaskAction {
    START,
    COMPLETE,
    SUBMIT_PUZZLE
  }
}
