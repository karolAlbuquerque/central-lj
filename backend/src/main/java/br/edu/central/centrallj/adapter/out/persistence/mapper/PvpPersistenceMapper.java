package br.edu.central.centrallj.adapter.out.persistence.mapper;

import br.edu.central.centrallj.adapter.out.persistence.entity.DuelSessionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.MissionMemberEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.MissionTaskEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.PlayerMissionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.PuzzleAttemptEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.SabotageEventEntity;
import br.edu.central.centrallj.domain.DuelSession;
import br.edu.central.centrallj.domain.MissionMember;
import br.edu.central.centrallj.domain.MissionTask;
import br.edu.central.centrallj.domain.PlayerMission;
import br.edu.central.centrallj.domain.PuzzleAttempt;
import br.edu.central.centrallj.domain.SabotageEvent;
import org.springframework.stereotype.Component;

@Component
public class PvpPersistenceMapper {

  public PlayerMission toDomain(PlayerMissionEntity e) {
    PlayerMission m = new PlayerMission();
    m.setId(e.getId());
    m.setTitulo(e.getTitulo());
    m.setDescricao(e.getDescricao());
    m.setOwnerUserId(e.getOwnerUserId());
    m.setCombatState(e.getCombatState());
    m.setMinPlayers(e.getMinPlayers());
    m.setPartySeed(e.getPartySeed());
    m.setStartedByUserId(e.getStartedByUserId());
    m.setStartedAt(e.getStartedAt());
    m.setCreatedAt(e.getCreatedAt());
    m.setUpdatedAt(e.getUpdatedAt());
    return m;
  }

  public PlayerMissionEntity toEntity(PlayerMission m) {
    PlayerMissionEntity e = new PlayerMissionEntity();
    e.setId(m.getId());
    e.setTitulo(m.getTitulo());
    e.setDescricao(m.getDescricao());
    e.setOwnerUserId(m.getOwnerUserId());
    e.setCombatState(m.getCombatState());
    e.setMinPlayers(m.getMinPlayers());
    e.setPartySeed(m.getPartySeed());
    e.setStartedByUserId(m.getStartedByUserId());
    e.setStartedAt(m.getStartedAt());
    e.setCreatedAt(m.getCreatedAt());
    e.setUpdatedAt(m.getUpdatedAt());
    return e;
  }

  public MissionMember toDomain(MissionMemberEntity e) {
    MissionMember m = new MissionMember();
    m.setId(e.getId());
    m.setMissionId(e.getMission().getId());
    m.setUserId(e.getUser().getId());
    m.setUserName(e.getUser().getNome());
    m.setRole(e.getRole());
    m.setInviteStatus(e.getInviteStatus());
    m.setJoinedAt(e.getJoinedAt());
    m.setInvitedByUserId(e.getInvitedByUserId());
    return m;
  }

  public MissionTask toDomain(MissionTaskEntity e) {
    MissionTask t = new MissionTask();
    t.setId(e.getId());
    t.setMissionId(e.getMission().getId());
    t.setAssignedToUserId(e.getAssignedToUserId());
    t.setTitle(e.getTitle());
    t.setDescription(e.getDescription());
    t.setStatus(e.getStatus());
    t.setCritical(e.isCritical());
    t.setPuzzleType(e.getPuzzleType());
    t.setDependsOnTaskId(e.getDependsOnTask() != null ? e.getDependsOnTask().getId() : null);
    t.setCreatedAt(e.getCreatedAt());
    t.setUpdatedAt(e.getUpdatedAt());
    return t;
  }

  public DuelSession toDomain(DuelSessionEntity e) {
    DuelSession d = new DuelSession();
    d.setId(e.getId());
    d.setMissionId(e.getMission().getId());
    d.setAttackerUserId(e.getAttackerUserId());
    d.setDefenderUserId(e.getDefenderUserId());
    d.setSeed(e.getSeed());
    d.setPuzzleType(e.getPuzzleType());
    d.setStatus(e.getStatus());
    d.setRoundCurrent(e.getRoundCurrent());
    d.setRoundMax(e.getRoundMax());
    d.setAttackerRoundsWon(e.getAttackerRoundsWon());
    d.setDefenderRoundsWon(e.getDefenderRoundsWon());
    d.setStartedAt(e.getStartedAt());
    d.setFinishedAt(e.getFinishedAt());
    d.setTimeoutAt(e.getTimeoutAt());
    d.setInfiltrationProgress(e.getInfiltrationProgress());
    return d;
  }

  public PuzzleAttempt toDomain(PuzzleAttemptEntity e) {
    PuzzleAttempt a = new PuzzleAttempt();
    a.setId(e.getId());
    a.setDuelSessionId(e.getDuelSession().getId());
    a.setUserId(e.getUserId());
    a.setRoundNumber(e.getRoundNumber());
    a.setMoveSequenceJson(e.getMoveSequence());
    a.setValid(e.getValid());
    a.setTimeMs(e.getTimeMs());
    a.setSubmittedAt(e.getSubmittedAt());
    return a;
  }

  public SabotageEvent toDomain(SabotageEventEntity e) {
    SabotageEvent s = new SabotageEvent();
    s.setId(e.getId());
    s.setDuelSessionId(e.getDuelSession().getId());
    s.setMissionId(e.getMission().getId());
    s.setAttackerUserId(e.getAttackerUserId());
    s.setSabotageType(e.getSabotageType());
    s.setTargetScope(e.getTargetScope());
    s.setTargetUserId(e.getTargetUserId());
    s.setAppliedAt(e.getAppliedAt());
    s.setExpiresAt(e.getExpiresAt());
    s.setReversedAt(e.getReversedAt());
    s.setEffectPayloadJson(e.getEffectPayload());
    return s;
  }
}
