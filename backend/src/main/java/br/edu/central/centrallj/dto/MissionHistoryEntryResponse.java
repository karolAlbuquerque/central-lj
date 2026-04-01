package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.MissionHistoryOrigin;
import br.edu.central.centrallj.domain.MissionStatus;
import java.time.Instant;
import java.util.UUID;

public record MissionHistoryEntryResponse(
    UUID id,
    MissionStatus statusAnterior,
    MissionStatus statusNovo,
    String mensagem,
    MissionHistoryOrigin origem,
    Instant ocorridoEm) {}
