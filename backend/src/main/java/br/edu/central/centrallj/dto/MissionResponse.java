package br.edu.central.centrallj.dto;

import br.edu.central.centrallj.domain.MissionStatus;
import br.edu.central.centrallj.domain.PrioridadeMissao;
import br.edu.central.centrallj.domain.TipoAmeaca;
import java.time.Instant;
import java.util.UUID;

public record MissionResponse(
    UUID id,
    String titulo,
    String descricao,
    TipoAmeaca tipoAmeaca,
    PrioridadeMissao prioridade,
    MissionStatus status,
    Instant dataCriacao,
    Instant ultimaAtualizacao,
    LocalizacaoResponse localizacao) {

  public record LocalizacaoResponse(String cidade, String bairro, String referencia) {}
}
