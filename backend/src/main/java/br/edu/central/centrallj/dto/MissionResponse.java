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
    LocalizacaoResponse localizacao,
    AtribuicaoResumo atribuicao) {

  public record LocalizacaoResponse(String cidade, String bairro, String referencia) {}

  /** Dados de designação atual (herói ou equipe), quando houver. */
  public record AtribuicaoResumo(
      UUID heroiId,
      String nomeHeroico,
      UUID equipeId,
      String nomeEquipe,
      Instant atribuidoEm,
      String atribuidoPor) {}
}
