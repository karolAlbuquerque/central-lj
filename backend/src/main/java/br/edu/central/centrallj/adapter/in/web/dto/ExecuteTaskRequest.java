package br.edu.central.centrallj.adapter.in.web.dto;

import br.edu.central.centrallj.application.model.ExecuteTaskCommand.TaskAction;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ExecuteTaskRequest(
    @NotNull TaskAction action,
    List<Integer> moves) {}
