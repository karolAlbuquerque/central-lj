package br.edu.central.centrallj.adapter.out.persistence.mapper;

import br.edu.central.centrallj.adapter.out.persistence.entity.EquipeHeroicaEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.HeroiEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.MissionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.MissionHistoryEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.UsuarioEntity;
import br.edu.central.centrallj.domain.EquipeHeroica;
import br.edu.central.centrallj.domain.Heroi;
import br.edu.central.centrallj.domain.Mission;
import br.edu.central.centrallj.domain.MissionHistory;
import br.edu.central.centrallj.domain.Usuario;
import org.springframework.stereotype.Component;

@Component
public class PersistenceEntityMapper {

  public Mission toDomain(MissionEntity e) {
    if (e == null) {
      return null;
    }
    Mission m = new Mission();
    m.setId(e.getId());
    m.setTitulo(e.getTitulo());
    m.setDescricao(e.getDescricao());
    m.setTipoAmeaca(e.getTipoAmeaca());
    m.setPrioridade(e.getPrioridade());
    m.setStatus(e.getStatus());
    m.setDataCriacao(e.getDataCriacao());
    m.setUltimaAtualizacao(e.getUltimaAtualizacao());
    m.setCidade(e.getCidade());
    m.setBairro(e.getBairro());
    m.setReferencia(e.getReferencia());
    m.setHeroiResponsavel(toDomainHeroiShallow(e.getHeroiResponsavel()));
    m.setEquipeResponsavel(toDomainEquipeShallow(e.getEquipeResponsavel()));
    m.setAtribuidoEm(e.getAtribuidoEm());
    m.setAtribuidoPor(e.getAtribuidoPor());
    return m;
  }

  public MissionEntity toEntity(Mission m) {
    if (m == null) {
      return null;
    }
    MissionEntity e = new MissionEntity();
    e.setId(m.getId());
    e.setTitulo(m.getTitulo());
    e.setDescricao(m.getDescricao());
    e.setTipoAmeaca(m.getTipoAmeaca());
    e.setPrioridade(m.getPrioridade());
    e.setStatus(m.getStatus());
    e.setDataCriacao(m.getDataCriacao());
    e.setUltimaAtualizacao(m.getUltimaAtualizacao());
    e.setCidade(m.getCidade());
    e.setBairro(m.getBairro());
    e.setReferencia(m.getReferencia());
    e.setHeroiResponsavel(toEntityHeroiRef(m.getHeroiResponsavel()));
    e.setEquipeResponsavel(toEntityEquipeRef(m.getEquipeResponsavel()));
    e.setAtribuidoEm(m.getAtribuidoEm());
    e.setAtribuidoPor(m.getAtribuidoPor());
    return e;
  }

  public MissionHistory toDomain(MissionHistoryEntity e) {
    if (e == null) {
      return null;
    }
    MissionHistory h = new MissionHistory();
    h.setId(e.getId());
    h.setMissionId(e.getMission() != null ? e.getMission().getId() : null);
    h.setStatusAnterior(e.getStatusAnterior());
    h.setStatusNovo(e.getStatusNovo());
    h.setMensagem(e.getMensagem());
    h.setOrigem(e.getOrigem());
    h.setOcorridoEm(e.getOcorridoEm());
    return h;
  }

  public MissionHistoryEntity toEntity(MissionHistory h) {
    if (h == null) {
      return null;
    }
    MissionHistoryEntity e = new MissionHistoryEntity();
    e.setId(h.getId());
    if (h.getMissionId() != null) {
      MissionEntity missionRef = new MissionEntity();
      missionRef.setId(h.getMissionId());
      e.setMission(missionRef);
    }
    e.setStatusAnterior(h.getStatusAnterior());
    e.setStatusNovo(h.getStatusNovo());
    e.setMensagem(h.getMensagem());
    e.setOrigem(h.getOrigem());
    e.setOcorridoEm(h.getOcorridoEm());
    return e;
  }

  public Heroi toDomain(HeroiEntity e) {
    if (e == null) {
      return null;
    }
    Heroi h = new Heroi();
    h.setId(e.getId());
    h.setNomeHeroico(e.getNomeHeroico());
    h.setNomeCivil(e.getNomeCivil());
    h.setEspecialidade(e.getEspecialidade());
    h.setStatusDisponibilidade(e.getStatusDisponibilidade());
    h.setNivel(e.getNivel());
    h.setAtivo(e.isAtivo());
    h.setEquipe(toDomainEquipeShallow(e.getEquipe()));
    h.setCreatedAt(e.getCreatedAt());
    h.setUpdatedAt(e.getUpdatedAt());
    return h;
  }

  public HeroiEntity toEntity(Heroi h) {
    if (h == null) {
      return null;
    }
    HeroiEntity e = new HeroiEntity();
    e.setId(h.getId());
    e.setNomeHeroico(h.getNomeHeroico());
    e.setNomeCivil(h.getNomeCivil());
    e.setEspecialidade(h.getEspecialidade());
    e.setStatusDisponibilidade(h.getStatusDisponibilidade());
    e.setNivel(h.getNivel());
    e.setAtivo(h.isAtivo());
    e.setEquipe(toEntityEquipeRef(h.getEquipe()));
    e.setCreatedAt(h.getCreatedAt());
    e.setUpdatedAt(h.getUpdatedAt());
    return e;
  }

  public EquipeHeroica toDomain(EquipeHeroicaEntity e) {
    if (e == null) {
      return null;
    }
    EquipeHeroica eq = new EquipeHeroica();
    eq.setId(e.getId());
    eq.setNome(e.getNome());
    eq.setEspecialidadePrincipal(e.getEspecialidadePrincipal());
    eq.setAtiva(e.isAtiva());
    eq.setCreatedAt(e.getCreatedAt());
    eq.setUpdatedAt(e.getUpdatedAt());
    return eq;
  }

  public EquipeHeroicaEntity toEntity(EquipeHeroica eq) {
    if (eq == null) {
      return null;
    }
    EquipeHeroicaEntity e = new EquipeHeroicaEntity();
    e.setId(eq.getId());
    e.setNome(eq.getNome());
    e.setEspecialidadePrincipal(eq.getEspecialidadePrincipal());
    e.setAtiva(eq.isAtiva());
    e.setCreatedAt(eq.getCreatedAt());
    e.setUpdatedAt(eq.getUpdatedAt());
    return e;
  }

  public Usuario toDomain(UsuarioEntity e) {
    if (e == null) {
      return null;
    }
    Usuario u = new Usuario();
    u.setId(e.getId());
    u.setNome(e.getNome());
    u.setEmail(e.getEmail());
    u.setSenhaHash(e.getSenhaHash());
    u.setRole(e.getRole());
    u.setAtivo(e.isAtivo());
    u.setHeroi(toDomainHeroiShallow(e.getHeroi()));
    u.setCooldownAvailableAt(e.getCooldownAvailableAt());
    u.setCreatedAt(e.getCreatedAt());
    u.setUpdatedAt(e.getUpdatedAt());
    return u;
  }

  public UsuarioEntity toEntity(Usuario u) {
    if (u == null) {
      return null;
    }
    UsuarioEntity e = new UsuarioEntity();
    e.setId(u.getId());
    e.setNome(u.getNome());
    e.setEmail(u.getEmail());
    e.setSenhaHash(u.getSenhaHash());
    e.setRole(u.getRole());
    e.setAtivo(u.isAtivo());
    e.setHeroi(toEntityHeroiRef(u.getHeroi()));
    e.setCooldownAvailableAt(u.getCooldownAvailableAt());
    e.setCreatedAt(u.getCreatedAt());
    e.setUpdatedAt(u.getUpdatedAt());
    return e;
  }

  private Heroi toDomainHeroiShallow(HeroiEntity e) {
    if (e == null) {
      return null;
    }
    Heroi h = new Heroi();
    h.setId(e.getId());
    h.setNomeHeroico(e.getNomeHeroico());
    return h;
  }

  private EquipeHeroica toDomainEquipeShallow(EquipeHeroicaEntity e) {
    if (e == null) {
      return null;
    }
    EquipeHeroica eq = new EquipeHeroica();
    eq.setId(e.getId());
    eq.setNome(e.getNome());
    return eq;
  }

  private HeroiEntity toEntityHeroiRef(Heroi h) {
    if (h == null || h.getId() == null) {
      return null;
    }
    HeroiEntity e = new HeroiEntity();
    e.setId(h.getId());
    return e;
  }

  private EquipeHeroicaEntity toEntityEquipeRef(EquipeHeroica eq) {
    if (eq == null || eq.getId() == null) {
      return null;
    }
    EquipeHeroicaEntity e = new EquipeHeroicaEntity();
    e.setId(eq.getId());
    return e;
  }
}
