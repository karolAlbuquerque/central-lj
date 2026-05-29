package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.application.exception.BadRequestException;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.model.DuelSessionView;
import br.edu.central.centrallj.application.model.SubmitProgressCommand;
import br.edu.central.centrallj.application.port.in.JoinDuelUseCase;
import br.edu.central.centrallj.application.port.in.ListVillainTargetsUseCase;
import br.edu.central.centrallj.application.port.in.StartInfiltrationUseCase;
import br.edu.central.centrallj.application.port.in.SubmitInfiltrationProgressUseCase;
import br.edu.central.centrallj.application.port.in.SubmitPuzzleProgressUseCase;
import br.edu.central.centrallj.application.model.PlayerMissionView;
import br.edu.central.centrallj.application.port.out.DuelSessionPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionMemberPersistencePort;
import br.edu.central.centrallj.application.port.out.PlayerMissionPersistencePort;
import br.edu.central.centrallj.application.port.out.PuzzleAttemptPersistencePort;
import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.DuelSession;
import br.edu.central.centrallj.domain.DuelStatus;
import br.edu.central.centrallj.domain.MissionCombatState;
import br.edu.central.centrallj.domain.MissionMemberRole;
import br.edu.central.centrallj.domain.PlayerMission;
import br.edu.central.centrallj.domain.PuzzleAttempt;
import br.edu.central.centrallj.domain.PuzzleType;
import br.edu.central.centrallj.domain.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DuelService
    implements StartInfiltrationUseCase,
        JoinDuelUseCase,
        SubmitInfiltrationProgressUseCase,
        SubmitPuzzleProgressUseCase,
        ListVillainTargetsUseCase {

  private static final List<DuelStatus> ACTIVE_STATUSES =
      List.of(DuelStatus.INFILTRATING, DuelStatus.PENDING, DuelStatus.ACTIVE);
  private static final int INFILTRATION_PUZZLES_REQUIRED = 3;
  private static final int DUEL_TIMEOUT_MINUTES = 3;
  private static final int MAX_INVALID_MOVES = 3;

  private final PlayerMissionPersistencePort missionPort;
  private final MissionMemberPersistencePort memberPort;
  private final DuelSessionPersistencePort duelPort;
  private final PuzzleAttemptPersistencePort attemptPort;
  private final UsuarioPersistencePort usuarioPort;
  private final PuzzleValidationService puzzleValidation;
  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  public DuelService(
      PlayerMissionPersistencePort missionPort,
      MissionMemberPersistencePort memberPort,
      DuelSessionPersistencePort duelPort,
      PuzzleAttemptPersistencePort attemptPort,
      UsuarioPersistencePort usuarioPort,
      PuzzleValidationService puzzleValidation,
      SimpMessagingTemplate messagingTemplate,
      ObjectMapper objectMapper) {
    this.missionPort = missionPort;
    this.memberPort = memberPort;
    this.duelPort = duelPort;
    this.attemptPort = attemptPort;
    this.usuarioPort = usuarioPort;
    this.puzzleValidation = puzzleValidation;
    this.messagingTemplate = messagingTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlayerMissionView> listTargets() {
    List<MissionCombatState> infiltrableStates =
        List.of(MissionCombatState.ACTIVE, MissionCombatState.NORMAL, MissionCombatState.EM_CRISE);
    return missionPort.findByStates(infiltrableStates).stream()
        .map(m -> new PlayerMissionView(
            m.getId(), m.getTitulo(), m.getDescricao(),
            m.getOwnerUserId(), m.getCombatState(),
            m.getMinPlayers(), m.getPartySeed(), m.getStartedByUserId(), m.getStartedAt(),
            m.getCreatedAt(), m.getUpdatedAt()))
        .toList();
  }

  @Override
  @Transactional
  public DuelSessionView start(UUID missionId, UUID attackerUserId) {
    var attacker =
        usuarioPort.findById(attackerUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    if (attacker.getRole() != UserRole.VILLAIN) {
      throw new BadRequestException("Apenas vilões podem iniciar infiltração.");
    }
    if (attacker.getCooldownAvailableAt() != null && Instant.now().isBefore(attacker.getCooldownAvailableAt())) {
      throw new BadRequestException("Você está em cooldown. Aguarde antes de infiltrar novamente.");
    }

    PlayerMission mission =
        missionPort.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    var existingInfiltrating =
        duelPort.findActiveByMission(missionId, List.of(DuelStatus.INFILTRATING));
    if (existingInfiltrating.isPresent()) {
      DuelSession existing = existingInfiltrating.get();
      if (existing.getAttackerUserId().equals(attackerUserId)) {
        return toView(existing);
      }
      throw new BadRequestException("Outro vilão já está infiltrando esta missão.");
    }

    var pendingIncomplete =
        duelPort.findActiveByMission(missionId, List.of(DuelStatus.PENDING));
    if (pendingIncomplete.isPresent()) {
      DuelSession pending = pendingIncomplete.get();
      if (pending.getInfiltrationProgress() < INFILTRATION_PUZZLES_REQUIRED) {
        if (pending.getAttackerUserId().equals(attackerUserId)) {
          pending.setStatus(DuelStatus.INFILTRATING);
          if (pending.getInfiltrationProgress() == 0) {
            String base = baseSeedForSession(pending);
            applyInfiltrationPuzzle(pending, base.isEmpty() ? UUID.randomUUID().toString().replace("-", "") : base, 1);
          } else {
            applyInfiltrationPuzzle(
                pending, baseSeedForSession(pending), pending.getInfiltrationProgress() + 1);
          }
          return toView(duelPort.save(pending));
        }
        throw new BadRequestException("Esta missão já possui um duelo em andamento.");
      }
    }

    if (duelPort.hasActiveSession(missionId, List.of(DuelStatus.PENDING, DuelStatus.ACTIVE))) {
      throw new BadRequestException("Esta missão já possui um duelo ativo.");
    }

    var chefeMember = memberPort.findByMission(missionId).stream()
        .filter(m -> m.getRole() == MissionMemberRole.CHEFE)
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Chefe da missão não encontrado."));

    Instant now = Instant.now();
    String baseSeed = UUID.randomUUID().toString().replace("-", "");
    DuelSession session = new DuelSession();
    session.setId(UUID.randomUUID());
    session.setMissionId(missionId);
    session.setAttackerUserId(attackerUserId);
    session.setDefenderUserId(chefeMember.getUserId());
    session.setInfiltrationProgress(0);
    applyInfiltrationPuzzle(session, baseSeed, 1);
    session.setStatus(DuelStatus.INFILTRATING);
    session.setRoundCurrent(1);
    session.setRoundMax(1);
    session.setAttackerRoundsWon(0);
    session.setDefenderRoundsWon(0);
    session.setTimeoutAt(now.plusSeconds(DUEL_TIMEOUT_MINUTES * 60L * 2L));

    DuelSession saved = duelPort.save(session);
    return toView(saved);
  }

  @Override
  @Transactional
  public DuelSessionView submitInfiltrationProgress(SubmitProgressCommand command) {
    DuelSession session =
        duelPort.findById(command.duelId())
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    if (session.getStatus() != DuelStatus.INFILTRATING) {
      throw new BadRequestException("Esta sessão não está em fase de infiltração.");
    }
    if (!session.getAttackerUserId().equals(command.userId())) {
      throw new BadRequestException("Apenas o vilão infiltrador pode enviar progresso.");
    }
    if (Instant.now().isAfter(session.getTimeoutAt())) {
      session.setStatus(DuelStatus.CANCELLED);
      session.setFinishedAt(Instant.now());
      duelPort.save(session);
      throw new BadRequestException("Tempo de infiltração esgotado.");
    }

    int puzzleIndex = session.getInfiltrationProgress() + 1;
    boolean isValid =
        puzzleValidation.validate(session.getSeed(), session.getPuzzleType(), command.moves());

    PuzzleAttempt attempt = new PuzzleAttempt();
    attempt.setId(UUID.randomUUID());
    attempt.setDuelSessionId(command.duelId());
    attempt.setUserId(command.userId());
    attempt.setRoundNumber(-puzzleIndex);
    attempt.setMoveSequenceJson(serializeMoves(command.moves()));
    attempt.setValid(isValid);
    attempt.setTimeMs((int) command.timeMs());
    attempt.setSubmittedAt(Instant.now());
    attemptPort.save(attempt);

    if (!isValid) {
      long invalidCount =
          attemptPort.countByDuelUserAndRound(command.duelId(), command.userId(), -puzzleIndex);
      if (invalidCount >= MAX_INVALID_MOVES) {
        session.setStatus(DuelStatus.CANCELLED);
        session.setFinishedAt(Instant.now());
        duelPort.save(session);
        throw new BadRequestException("Infiltração falhou — muitos erros nos puzzles de brecha.");
      }
      return toView(session);
    }

    session.setInfiltrationProgress(puzzleIndex);
    if (puzzleIndex < INFILTRATION_PUZZLES_REQUIRED) {
      applyInfiltrationPuzzle(session, baseSeedForSession(session), puzzleIndex + 1);
      DuelSession saved = duelPort.save(session);
      return toView(saved);
    }

    return completeInfiltrationAndAlertDefenders(session);
  }

  private DuelSessionView completeInfiltrationAndAlertDefenders(DuelSession session) {
    Instant now = Instant.now();
    String duelSeed = UUID.randomUUID().toString().replace("-", "");
    session.setSeed(duelSeed);
    session.setPuzzleType(puzzleTypeForSeed(duelSeed));
    session.setStatus(DuelStatus.PENDING);
    session.setRoundCurrent(1);
    session.setTimeoutAt(now.plusSeconds(DUEL_TIMEOUT_MINUTES * 60L));

    DuelSession saved = duelPort.save(session);

    PlayerMission mission =
        missionPort
            .findById(session.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    mission.setCombatState(MissionCombatState.ALERTA_INFILTRACAO);
    mission.setUpdatedAt(now);
    missionPort.save(mission);

    DuelSessionView view = toView(saved);
    messagingTemplate.convertAndSendToUser(
        session.getDefenderUserId().toString(), "/queue/duel-invite", view);
    messagingTemplate.convertAndSend(
        "/topic/mission/" + session.getMissionId(),
        MissionWsMessage.infiltrationAlert(
            session.getMissionId(), session.getId(), session.getAttackerUserId()));

    return view;
  }

  private String baseSeedForSession(DuelSession session) {
    String s = session.getSeed();
    int idx = s.indexOf("-inf-");
    return idx > 0 ? s.substring(0, idx) : s;
  }

  private void applyInfiltrationPuzzle(DuelSession session, String baseSeed, int puzzleIndex) {
    String seed = baseSeed + "-inf-" + puzzleIndex;
    session.setSeed(seed);
    session.setPuzzleType(infiltrationPuzzleTypeForSeed(seed));
  }

  /** Puzzles mais estáveis na fase de brecha (3 etapas antes do alerta). */
  private PuzzleType infiltrationPuzzleTypeForSeed(String seed) {
    String normalized = seed.contains("-inf-") ? seed.substring(0, seed.indexOf("-inf-")) : seed;
    String hex = normalized.substring(0, Math.min(8, normalized.length()));
    long state = Long.parseLong(hex, 16) & 0xFFFFFFFFL;
    state = (state * 1664525L + 1013904223L) & 0xFFFFFFFFL;
    PuzzleType[] types = {
      PuzzleType.SEQUENCE_INPUT, PuzzleType.DRAG_SORT, PuzzleType.NODE_CONNECT
    };
    return types[(int) (state % types.length)];
  }

  @Override
  @Transactional
  public DuelSessionView join(UUID duelId, UUID defenderUserId) {
    DuelSession session =
        duelPort.findById(duelId)
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    if (session.getStatus() != DuelStatus.PENDING) {
      throw new BadRequestException("Este duelo não está aguardando participação.");
    }
    if (!session.getDefenderUserId().equals(defenderUserId)) {
      throw new BadRequestException("Você não é o defensor deste duelo.");
    }

    Instant now = Instant.now();
    session.setStatus(DuelStatus.ACTIVE);
    session.setStartedAt(now);
    session.setTimeoutAt(now.plusSeconds(DUEL_TIMEOUT_MINUTES * 60L));

    DuelSession saved = duelPort.save(session);

    PlayerMission mission = missionPort.findById(session.getMissionId()).orElseThrow();
    mission.setCombatState(MissionCombatState.EM_DUELO);
    mission.setUpdatedAt(now);
    missionPort.save(mission);

    messagingTemplate.convertAndSend(
        "/topic/duel/" + duelId,
        DuelWsMessage.started(saved.getSeed(), saved.getPuzzleType().name(), saved.getRoundMax(), saved.getTimeoutAt()));

    return toView(saved);
  }

  @Override
  @Transactional
  public DuelSessionView submit(SubmitProgressCommand command) {
    DuelSession session =
        duelPort.findById(command.duelId())
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    if (session.getStatus() != DuelStatus.ACTIVE) {
      throw new BadRequestException("Duelo não está ativo.");
    }
    if (Instant.now().isAfter(session.getTimeoutAt())) {
      return resolveTimeout(session);
    }

    boolean isValid = puzzleValidation.validate(session.getSeed(), session.getPuzzleType(), command.moves());

    PuzzleAttempt attempt = new PuzzleAttempt();
    attempt.setId(UUID.randomUUID());
    attempt.setDuelSessionId(command.duelId());
    attempt.setUserId(command.userId());
    attempt.setRoundNumber(command.roundNumber());
    attempt.setMoveSequenceJson(serializeMoves(command.moves()));
    attempt.setValid(isValid);
    attempt.setTimeMs((int) command.timeMs());
    attempt.setSubmittedAt(Instant.now());
    attemptPort.save(attempt);

    if (!isValid) {
      long invalidCount = attemptPort.countByDuelUserAndRound(command.duelId(), command.userId(), command.roundNumber());
      if (invalidCount >= MAX_INVALID_MOVES) {
        return resolveRound(session, command.userId(), false);
      }
      messagingTemplate.convertAndSendToUser(
          command.userId().toString(), "/queue/errors",
          "Movimento inválido. Tentativas restantes: " + (MAX_INVALID_MOVES - invalidCount));
      return toView(session);
    }

    return resolveRound(session, command.userId(), true);
  }

  private DuelSessionView resolveRound(DuelSession session, UUID winnerId, boolean valid) {
    boolean attackerWon = valid && winnerId.equals(session.getAttackerUserId());
    boolean defenderWon = valid && winnerId.equals(session.getDefenderUserId());

    if (attackerWon) session.setAttackerRoundsWon(session.getAttackerRoundsWon() + 1);
    if (defenderWon) session.setDefenderRoundsWon(session.getDefenderRoundsWon() + 1);

    int winThreshold = (session.getRoundMax() / 2) + 1;
    boolean duelOver = session.getAttackerRoundsWon() >= winThreshold
        || session.getDefenderRoundsWon() >= winThreshold;

    if (duelOver) {
      boolean attackerFinalWin = session.getAttackerRoundsWon() >= winThreshold;
      session.setStatus(attackerFinalWin ? DuelStatus.VILLAIN_WON : DuelStatus.HERO_WON);
      session.setFinishedAt(Instant.now());
      DuelSession saved = duelPort.save(session);
      resolveMissionAfterDuel(saved, attackerFinalWin);
      messagingTemplate.convertAndSend(
          "/topic/duel/" + session.getId(),
          DuelWsMessage.ended(attackerFinalWin ? session.getAttackerUserId() : session.getDefenderUserId()));
      return toView(saved);
    }

    session.setRoundCurrent(session.getRoundCurrent() + 1);
    DuelSession saved = duelPort.save(session);
    messagingTemplate.convertAndSend(
        "/topic/duel/" + session.getId(),
        DuelWsMessage.roundEnd(session.getRoundCurrent() - 1, winnerId));
    return toView(saved);
  }

  private DuelSessionView resolveTimeout(DuelSession session) {
    session.setStatus(DuelStatus.TIMEOUT);
    session.setFinishedAt(Instant.now());
    DuelSession saved = duelPort.save(session);
    resolveMissionAfterDuel(saved, true);
    messagingTemplate.convertAndSend("/topic/duel/" + session.getId(), DuelWsMessage.cancelled("Timeout"));
    return toView(saved);
  }

  private void resolveMissionAfterDuel(DuelSession session, boolean villainWon) {
    missionPort.findById(session.getMissionId()).ifPresent(mission -> {
      mission.setCombatState(villainWon ? MissionCombatState.SABOTADA : MissionCombatState.DEFENDIDA);
      mission.setUpdatedAt(Instant.now());
      missionPort.save(mission);
    });
  }

  private String serializeMoves(List<Integer> moves) {
    try {
      return objectMapper.writeValueAsString(moves);
    } catch (JsonProcessingException e) {
      return "[]";
    }
  }

  private PuzzleType puzzleTypeForSeed(String seed) {
    String normalized = seed.contains("-inf-") ? seed.substring(0, seed.indexOf("-inf-")) : seed;
    String hex = normalized.substring(0, Math.min(8, normalized.length()));
    long state = Long.parseLong(hex, 16) & 0xFFFFFFFFL;
    state = (state * 1664525L + 1013904223L) & 0xFFFFFFFFL;
    PuzzleType[] types = PuzzleType.values();
    return types[(int) (state % types.length)];
  }

  private DuelSessionView toView(DuelSession d) {
    return new DuelSessionView(
        d.getId(), d.getMissionId(), d.getAttackerUserId(), d.getDefenderUserId(),
        d.getSeed(), d.getPuzzleType(), d.getStatus(),
        d.getRoundCurrent(), d.getRoundMax(),
        d.getAttackerRoundsWon(), d.getDefenderRoundsWon(),
        d.getInfiltrationProgress(), INFILTRATION_PUZZLES_REQUIRED,
        d.getStartedAt(), d.getFinishedAt(), d.getTimeoutAt());
  }

  record MissionWsMessage(String type, Object payload) {
    static MissionWsMessage infiltrationAlert(UUID missionId, UUID duelId, UUID attackerUserId) {
      return new MissionWsMessage(
          "mission:infiltration-alert",
          java.util.Map.of(
              "missionId", missionId.toString(),
              "duelId", duelId.toString(),
              "attackerUserId", attackerUserId.toString()));
    }
  }

  record DuelWsMessage(String type, Object payload) {
    static DuelWsMessage started(String seed, String puzzleType, int roundMax, Instant timeoutAt) {
      return new DuelWsMessage("duel:started",
          java.util.Map.of("seed", seed, "puzzleType", puzzleType, "roundMax", roundMax, "timeoutAt", timeoutAt.toString()));
    }
    static DuelWsMessage roundEnd(int round, UUID winnerId) {
      return new DuelWsMessage("duel:round-end",
          java.util.Map.of("roundNumber", round, "winnerId", winnerId.toString()));
    }
    static DuelWsMessage ended(UUID winnerId) {
      return new DuelWsMessage("duel:ended", java.util.Map.of("winnerId", winnerId.toString()));
    }
    static DuelWsMessage cancelled(String reason) {
      return new DuelWsMessage("duel:cancelled", java.util.Map.of("reason", reason));
    }
  }
}
