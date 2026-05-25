package br.edu.central.centrallj.adapter.in.web.mapper;

import br.edu.central.centrallj.adapter.in.web.dto.AssignHeroRequest;
import br.edu.central.centrallj.adapter.in.web.dto.AssignTeamRequest;
import br.edu.central.centrallj.adapter.in.web.dto.AuthUserResponse;
import br.edu.central.centrallj.adapter.in.web.dto.CreateEquipeRequest;
import br.edu.central.centrallj.adapter.in.web.dto.CreateHeroRequest;
import br.edu.central.centrallj.adapter.in.web.dto.CreateMissionRequest;
import br.edu.central.centrallj.adapter.in.web.dto.DashboardSummaryResponse;
import br.edu.central.centrallj.adapter.in.web.dto.EquipeDetailResponse;
import br.edu.central.centrallj.adapter.in.web.dto.EquipeResponse;
import br.edu.central.centrallj.adapter.in.web.dto.HeroDetailResponse;
import br.edu.central.centrallj.adapter.in.web.dto.HeroResponse;
import br.edu.central.centrallj.adapter.in.web.dto.LoginRequest;
import br.edu.central.centrallj.adapter.in.web.dto.LoginResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionDetailResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionHistoryEntryResponse;
import br.edu.central.centrallj.adapter.in.web.dto.MissionResponse;
import br.edu.central.centrallj.adapter.in.web.dto.PatchHeroAvailabilityRequest;
import br.edu.central.centrallj.application.model.AssignHeroCommand;
import br.edu.central.centrallj.application.model.AssignTeamCommand;
import br.edu.central.centrallj.application.model.AuthUserView;
import br.edu.central.centrallj.application.model.CreateEquipeCommand;
import br.edu.central.centrallj.application.model.CreateHeroCommand;
import br.edu.central.centrallj.application.model.CreateMissionCommand;
import br.edu.central.centrallj.application.model.DashboardSummaryView;
import br.edu.central.centrallj.application.model.EquipeDetailView;
import br.edu.central.centrallj.application.model.EquipeView;
import br.edu.central.centrallj.application.model.HeroDetailView;
import br.edu.central.centrallj.application.model.HeroView;
import br.edu.central.centrallj.application.model.LoginCommand;
import br.edu.central.centrallj.application.model.LoginResult;
import br.edu.central.centrallj.application.model.MissionDetailView;
import br.edu.central.centrallj.application.model.MissionHistoryEntryView;
import br.edu.central.centrallj.application.model.MissionView;
import br.edu.central.centrallj.application.model.PatchHeroAvailabilityCommand;
import org.springframework.stereotype.Component;

@Component
public class WebDtoMapper {

  public CreateMissionCommand toCommand(CreateMissionRequest r) {
    return new CreateMissionCommand(
        r.titulo(),
        r.descricao(),
        r.tipoAmeaca(),
        r.prioridade(),
        r.cidade(),
        r.bairro(),
        r.referencia());
  }

  public AssignHeroCommand toCommand(AssignHeroRequest r) {
    return new AssignHeroCommand(r.heroiId(), r.atribuidoPor());
  }

  public AssignTeamCommand toCommand(AssignTeamRequest r) {
    return new AssignTeamCommand(r.equipeId(), r.atribuidoPor());
  }

  public CreateHeroCommand toCommand(CreateHeroRequest r) {
    return new CreateHeroCommand(
        r.nomeHeroico(),
        r.nomeCivil(),
        r.especialidade(),
        r.statusDisponibilidade(),
        r.nivel(),
        r.ativo(),
        r.equipeId());
  }

  public CreateEquipeCommand toCommand(CreateEquipeRequest r) {
    return new CreateEquipeCommand(r.nome(), r.especialidadePrincipal(), r.ativa());
  }

  public PatchHeroAvailabilityCommand toCommand(PatchHeroAvailabilityRequest r) {
    return new PatchHeroAvailabilityCommand(r.disponibilidade());
  }

  public LoginCommand toCommand(LoginRequest r) {
    return new LoginCommand(r.email(), r.password());
  }

  public MissionResponse toDto(MissionView v) {
    MissionResponse.LocalizacaoResponse loc = null;
    if (v.localizacao() != null) {
      loc =
          new MissionResponse.LocalizacaoResponse(
              v.localizacao().cidade(), v.localizacao().bairro(), v.localizacao().referencia());
    }
    MissionResponse.AtribuicaoResumo atr = null;
    if (v.atribuicao() != null) {
      atr =
          new MissionResponse.AtribuicaoResumo(
              v.atribuicao().heroiId(),
              v.atribuicao().heroiNome(),
              v.atribuicao().equipeId(),
              v.atribuicao().equipeNome(),
              v.atribuicao().atribuidoEm(),
              v.atribuicao().atribuidoPor());
    }
    return new MissionResponse(
        v.id(),
        v.titulo(),
        v.descricao(),
        v.tipoAmeaca(),
        v.prioridade(),
        v.status(),
        v.dataCriacao(),
        v.ultimaAtualizacao(),
        loc,
        atr);
  }

  public MissionHistoryEntryResponse toDto(MissionHistoryEntryView v) {
    return new MissionHistoryEntryResponse(
        v.id(), v.statusAnterior(), v.statusNovo(), v.mensagem(), v.origem(), v.ocorridoEm());
  }

  public MissionDetailResponse toDto(MissionDetailView v) {
    return new MissionDetailResponse(
        toDto(v.missao()), v.historico().stream().map(this::toDto).toList());
  }

  public DashboardSummaryResponse toDto(DashboardSummaryView v) {
    return new DashboardSummaryResponse(
        v.total(),
        v.emAndamento(),
        v.concluidas(),
        v.falhas(),
        v.recentes().stream().map(this::toDto).toList());
  }

  public HeroResponse toDto(HeroView v) {
    HeroResponse.EquipeResumo equipe =
        v.equipeId() != null
            ? new HeroResponse.EquipeResumo(v.equipeId(), v.equipeNome())
            : null;
    return new HeroResponse(
        v.id(),
        v.nomeHeroico(),
        v.nomeCivil(),
        v.especialidade(),
        v.statusDisponibilidade(),
        v.nivel(),
        v.ativo(),
        equipe,
        v.createdAt(),
        v.updatedAt());
  }

  public HeroDetailResponse toDto(HeroDetailView v) {
    return new HeroDetailResponse(
        toDto(v.heroi()), v.missoes().stream().map(this::toDto).toList());
  }

  public EquipeResponse toDto(EquipeView v) {
    return new EquipeResponse(
        v.id(), v.nome(), v.especialidadePrincipal(), v.ativa(), v.createdAt(), v.updatedAt());
  }

  public EquipeDetailResponse toDto(EquipeDetailView v) {
    return new EquipeDetailResponse(
        toDto(v.equipe()), v.herois().stream().map(this::toDto).toList());
  }

  public LoginResponse toDto(LoginResult r) {
    return new LoginResponse(r.accessToken(), r.tokenType(), toDto(r.user()));
  }

  public AuthUserResponse toDto(AuthUserView v) {
    return new AuthUserResponse(v.id(), v.nome(), v.email(), v.role(), v.heroiId());
  }
}
