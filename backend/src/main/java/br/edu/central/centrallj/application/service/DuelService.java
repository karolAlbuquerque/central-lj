package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.application.exception.BadRequestException;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.model.DuelSessionView;
import br.edu.central.centrallj.application.model.SubmitProgressCommand;
import br.edu.central.centrallj.application.port.in.GetDuelSessionUseCase;
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
import br.edu.central.centrallj.application.port.out.MissionNotificationPort;
import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.Usuario;
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
        GetDuelSessionUseCase,
        SubmitInfiltrationProgressUseCase,
        SubmitPuzzleProgressUseCase,
        ListVillainTargetsUseCase {

  private static final List<DuelStatus> ACTIVE_STATUSES =
      List.of(DuelStatus.INFILTRATING, DuelStatus.PENDING, DuelStatus.ACTIVE);
  private static final int INFILTRATION_PUZZLES_REQUIRED = 3;
  private static final int DUEL_PUZZLES_REQUIRED = 3;
  private static final int DUEL_TIMEOUT_MINUTES = 3;
  private static final int HERO_ACCEPT_DUEL_MINUTES = 120;
  private static final int MAX_INVALID_MOVES = 3;
  private static final String VILLAIN_SIDE = "vil";
  private static final String HERO_SIDE = "her";
  private static final PuzzleType[] DUEL_PUZZLE_TYPES =
      {PuzzleType.SEQUENCE_INPUT, PuzzleType.DRAG_SORT, PuzzleType.NODE_CONNECT};
  private static final int[][] DUEL_TYPE_PERMUTATIONS = {
    {0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}
  };

  private final PlayerMissionPersistencePort missionPort;
  private final MissionMemberPersistencePort memberPort;
  private final DuelSessionPersistencePort duelPort;
  private final PuzzleAttemptPersistencePort attemptPort;
  private final UsuarioPersistencePort usuarioPort;
  private final MissionNotificationPort missionNotificationPort;
  private final PuzzleValidationService puzzleValidation;
  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  public DuelService(
      PlayerMissionPersistencePort missionPort,
      MissionMemberPersistencePort memberPort,
      DuelSessionPersistencePort duelPort,
      PuzzleAttemptPersistencePort attemptPort,
      UsuarioPersistencePort usuarioPort,
      MissionNotificationPort missionNotificationPort,
      PuzzleValidationService puzzleValidation,
      SimpMessagingTemplate messagingTemplate,
      ObjectMapper objectMapper) {
    this.missionPort = missionPort;
    this.memberPort = memberPort;
    this.duelPort = duelPort;
    this.attemptPort = attemptPort;
    this.usuarioPort = usuarioPort;
    this.missionNotificationPort = missionNotificationPort;
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
    assertMissionAllowsInfiltration(mission);
    var existingInfiltrating =
        duelPort.findActiveByMission(missionId, List.of(DuelStatus.INFILTRATING));
    if (existingInfiltrating.isPresent()) {
      DuelSession existing = existingInfiltrating.get();
      if (existing.getAttackerUserId().equals(attackerUserId)) {
        return toView(existing, attackerUserId);
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
          return toView(duelPort.save(pending), attackerUserId);
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
    session.setRoundMax(DUEL_PUZZLES_REQUIRED);
    session.setAttackerRoundsWon(0);
    session.setDefenderRoundsWon(0);
    session.setTimeoutAt(now.plusSeconds(DUEL_TIMEOUT_MINUTES * 60L * 2L));

    DuelSession saved = duelPort.save(session);
    mission.setCombatState(MissionCombatState.ALERTA_INFILTRACAO);
    mission.setUpdatedAt(now);
    missionPort.save(mission);
    missionNotificationPort.notifyMissionUpdate(missionId);
    return toView(saved, attackerUserId);
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
      return toView(session, command.userId());
    }

    session.setInfiltrationProgress(puzzleIndex);
    if (puzzleIndex < INFILTRATION_PUZZLES_REQUIRED) {
      applyInfiltrationPuzzle(session, baseSeedForSession(session), puzzleIndex + 1);
      DuelSession saved = duelPort.save(session);
      return toView(saved, command.userId());
    }

    return completeInfiltrationAwaitingHero(session, command.userId());
  }

  /** Após as 3 brechas: duelo fica PENDING até o herói (defensor) aceitar na arena. */
  private DuelSessionView completeInfiltrationAwaitingHero(DuelSession session, UUID attackerUserId) {
    Instant now = Instant.now();
    String duelSeed = UUID.randomUUID().toString().replace("-", "");
    session.setSeed(duelSeed);
    session.setStatus(DuelStatus.PENDING);
    session.setRoundCurrent(1);
    session.setRoundMax(DUEL_PUZZLES_REQUIRED);
    session.setAttackerRoundsWon(0);
    session.setDefenderRoundsWon(0);
    session.setStartedAt(null);
    session.setTimeoutAt(now.plusSeconds(HERO_ACCEPT_DUEL_MINUTES * 60L));

    DuelSession saved = duelPort.save(session);

    PlayerMission mission =
        missionPort
            .findById(session.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    mission.setCombatState(MissionCombatState.ALERTA_INFILTRACAO);
    mission.setUpdatedAt(now);
    missionPort.save(mission);
    missionNotificationPort.notifyMissionUpdate(mission.getId());

    DuelSessionView defenderView = toView(saved, session.getDefenderUserId());
    messagingTemplate.convertAndSendToUser(
        session.getDefenderUserId().toString(), "/queue/duel-invite", defenderView);
    messagingTemplate.convertAndSend(
        "/topic/mission/" + session.getMissionId(),
        MissionWsMessage.infiltrationAlert(
            session.getMissionId(), session.getId(), session.getAttackerUserId()));
    messagingTemplate.convertAndSend(
        "/topic/duel/" + session.getId(),
        DuelWsMessage.pendingHeroAccept(session.getTimeoutAt()));

    return toView(saved, attackerUserId);
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

  /** Puzzles da fase de brecha — garante tipos distintos por índice de puzzle. */
  private PuzzleType infiltrationPuzzleTypeForSeed(String seed) {
    int infIdx = seed.indexOf("-inf-");
    String base = infIdx > 0 ? seed.substring(0, infIdx) : seed;
    int puzzleIndex = 1;
    if (infIdx > 0) {
      try { puzzleIndex = Integer.parseInt(seed.substring(infIdx + 5)); } catch (NumberFormatException ignored) {}
    }
    String hex = base.substring(0, Math.min(8, base.length()));
    long state = Long.parseLong(hex, 16) & 0xFFFFFFFFL;
    state = (state * 1664525L + 1013904223L) & 0xFFFFFFFFL;
    int offset = (int) (state % 3);
    PuzzleType[] types = {PuzzleType.SEQUENCE_INPUT, PuzzleType.DRAG_SORT, PuzzleType.NODE_CONNECT};
    return types[(offset + puzzleIndex - 1) % 3];
  }

  @Override
  @Transactional(readOnly = true)
  public DuelSessionView getForUser(UUID duelId, UUID userId) {
    DuelSession session =
        duelPort.findById(duelId)
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    return toView(session, userId);
  }

  @Override
  @Transactional
  public DuelSessionView join(UUID duelId, UUID defenderUserId) {
    DuelSession session =
        duelPort.findById(duelId)
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    if (session.getAttackerUserId().equals(defenderUserId)) {
      throw new BadRequestException("Apenas o herói defensor pode aceitar o duelo.");
    }
    if (!session.getDefenderUserId().equals(defenderUserId)) {
      throw new BadRequestException("Você não é o defensor deste duelo.");
    }
    if (session.getStatus() == DuelStatus.ACTIVE) {
      return toView(session, defenderUserId);
    }
    if (session.getStatus() != DuelStatus.PENDING) {
      throw new BadRequestException("Este duelo não está aguardando aceite do herói.");
    }
    if (Instant.now().isAfter(session.getTimeoutAt())) {
      cancelPendingDuel(session);
      throw new BadRequestException("O prazo para aceitar o duelo expirou.");
    }
    DuelSession saved = activatePendingDuel(session);
    missionNotificationPort.notifyMissionUpdate(saved.getMissionId());
    messagingTemplate.convertAndSend(
        "/topic/duel/" + saved.getId(),
        DuelWsMessage.started(
            saved.getSeed(),
            duelPuzzleTypeForPlayer(saved.getSeed(), VILLAIN_SIDE, 1).name(),
            DUEL_PUZZLES_REQUIRED,
            saved.getTimeoutAt()));
    return toView(saved, defenderUserId);
  }

  private DuelSession activatePendingDuel(DuelSession session) {
    Instant now = Instant.now();
    session.setStatus(DuelStatus.ACTIVE);
    session.setRoundMax(DUEL_PUZZLES_REQUIRED);
    session.setAttackerRoundsWon(0);
    session.setDefenderRoundsWon(0);
    session.setStartedAt(now);
    session.setTimeoutAt(now.plusSeconds(DUEL_TIMEOUT_MINUTES * 60L));
    DuelSession saved = duelPort.save(session);
    missionPort
        .findById(session.getMissionId())
        .ifPresent(
            mission -> {
              mission.setCombatState(MissionCombatState.EM_DUELO);
              mission.setUpdatedAt(now);
              missionPort.save(mission);
            });
    return saved;
  }

  private void cancelPendingDuel(DuelSession session) {
    session.setStatus(DuelStatus.CANCELLED);
    session.setFinishedAt(Instant.now());
    duelPort.save(session);
    missionPort
        .findById(session.getMissionId())
        .ifPresent(
            mission -> {
              if (mission.getCombatState() == MissionCombatState.ALERTA_INFILTRACAO
                  || mission.getCombatState() == MissionCombatState.EM_DUELO) {
                mission.setCombatState(MissionCombatState.NORMAL);
                mission.setUpdatedAt(Instant.now());
                missionPort.save(mission);
                missionNotificationPort.notifyMissionUpdate(mission.getId());
              }
            });
  }

  private void assertMissionAllowsInfiltration(PlayerMission mission) {
    MissionCombatState state = mission.getCombatState();
    if (state == MissionCombatState.DERROTADA
        || state == MissionCombatState.COMPROMETIDA
        || state == MissionCombatState.SABOTADA) {
      throw new BadRequestException("Esta missão já foi encerrada.");
    }
    if (state == MissionCombatState.LOBBY) {
      throw new BadRequestException("A missão ainda não foi iniciada pelo herói.");
    }
  }

  @Override
  @Transactional
  public DuelSessionView submit(SubmitProgressCommand command) {
    DuelSession session =
        duelPort.findById(command.duelId())
            .orElseThrow(() -> new ResourceNotFoundException("Duelo não encontrado."));
    if (session.getStatus() == DuelStatus.PENDING) {
      throw new BadRequestException("O herói ainda não aceitou o duelo. Aguarde o defensor entrar na arena.");
    }
    if (session.getStatus() != DuelStatus.ACTIVE) {
      throw new BadRequestException("Duelo não está ativo.");
    }
    if (Instant.now().isAfter(session.getTimeoutAt())) {
      return resolveTimeout(session, command.userId());
    }

    boolean isAttacker = session.getAttackerUserId().equals(command.userId());
    boolean isDefender = session.getDefenderUserId().equals(command.userId());
    if (!isAttacker && !isDefender) {
      throw new BadRequestException("Você não participa deste duelo.");
    }

    int completed = isAttacker ? session.getAttackerRoundsWon() : session.getDefenderRoundsWon();
    if (completed >= DUEL_PUZZLES_REQUIRED) {
      throw new BadRequestException("Você já concluiu todos os puzzles deste duelo.");
    }

    int puzzleIndex = completed + 1;
    if (command.roundNumber() != puzzleIndex) {
      throw new BadRequestException("Índice de puzzle inválido para o seu progresso atual.");
    }

    String side = isAttacker ? VILLAIN_SIDE : HERO_SIDE;
    String baseSeed = session.getSeed();
    String puzzleSeed = duelSeedForPlayer(baseSeed, side, puzzleIndex);
    PuzzleType puzzleType = duelPuzzleTypeForPlayer(baseSeed, side, puzzleIndex);
    boolean isValid = puzzleValidation.validate(puzzleSeed, puzzleType, command.moves());

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
      long invalidCount =
          attemptPort.countByDuelUserAndRound(command.duelId(), command.userId(), command.roundNumber());
      if (invalidCount >= MAX_INVALID_MOVES) {
        throw new BadRequestException("Muitas tentativas inválidas neste puzzle.");
      }
      messagingTemplate.convertAndSendToUser(
          command.userId().toString(), "/queue/errors",
          "Movimento inválido. Tentativas restantes: " + (MAX_INVALID_MOVES - invalidCount));
      return toView(session, command.userId());
    }

    if (isAttacker) {
      session.setAttackerRoundsWon(session.getAttackerRoundsWon() + 1);
    } else {
      session.setDefenderRoundsWon(session.getDefenderRoundsWon() + 1);
    }

    if (session.getAttackerRoundsWon() >= DUEL_PUZZLES_REQUIRED) {
      return finishDuel(session, true, command.userId());
    }
    if (session.getDefenderRoundsWon() >= DUEL_PUZZLES_REQUIRED) {
      return finishDuel(session, false, command.userId());
    }

    DuelSession saved = duelPort.save(session);
    messagingTemplate.convertAndSend(
        "/topic/duel/" + session.getId(),
        DuelWsMessage.scoreUpdate(saved.getAttackerRoundsWon(), saved.getDefenderRoundsWon()));
    return toView(saved, command.userId());
  }

  private DuelSessionView finishDuel(DuelSession session, boolean villainWon, UUID viewerId) {
    session.setStatus(villainWon ? DuelStatus.VILLAIN_WON : DuelStatus.HERO_WON);
    session.setFinishedAt(Instant.now());
    DuelSession saved = duelPort.save(session);
    resolveMissionAfterDuel(saved, villainWon);
    messagingTemplate.convertAndSend(
        "/topic/duel/" + session.getId(),
        DuelWsMessage.ended(villainWon ? session.getAttackerUserId() : session.getDefenderUserId()));
    return toView(saved, viewerId);
  }

  private DuelSessionView resolveTimeout(DuelSession session, UUID viewerId) {
    boolean villainWins = session.getAttackerRoundsWon() > session.getDefenderRoundsWon();
    session.setStatus(DuelStatus.TIMEOUT);
    session.setFinishedAt(Instant.now());
    DuelSession saved = duelPort.save(session);
    resolveMissionAfterDuel(saved, villainWins);
    messagingTemplate.convertAndSend(
        "/topic/duel/" + session.getId(), DuelWsMessage.cancelled("Timeout"));
    return toView(saved, viewerId);
  }

  private void resolveMissionAfterDuel(DuelSession session, boolean villainWon) {
    missionPort
        .findById(session.getMissionId())
        .ifPresent(
            mission -> {
              mission.setCombatState(
                  villainWon ? MissionCombatState.DERROTADA : MissionCombatState.NORMAL);
              mission.setUpdatedAt(Instant.now());
              missionPort.save(mission);
              missionNotificationPort.notifyMissionUpdate(mission.getId());
            });
  }

  private String displayName(UUID userId) {
    return usuarioPort
        .findById(userId)
        .map(Usuario::getNome)
        .filter(n -> n != null && !n.isBlank())
        .orElse("Jogador");
  }

  private String serializeMoves(List<Integer> moves) {
    try {
      return objectMapper.writeValueAsString(moves);
    } catch (JsonProcessingException e) {
      return "[]";
    }
  }

  private String duelSeedForPlayer(String baseSeed, String side, int puzzleIndex) {
    return baseSeed + "-" + side + "-" + puzzleIndex;
  }

  private PuzzleType duelPuzzleTypeForPlayer(String baseSeed, String side, int puzzleIndex) {
    int perm = duelPermutationIndex(baseSeed, side);
    int[] order = DUEL_TYPE_PERMUTATIONS[perm];
    return DUEL_PUZZLE_TYPES[order[puzzleIndex - 1]];
  }

  private int duelPermutationIndex(String baseSeed, String side) {
    String hex = hashHex(baseSeed + "-deck-" + side);
    long state = Long.parseLong(hex, 16) & 0xFFFFFFFFL;
    state = (state * 1664525L + 1013904223L) & 0xFFFFFFFFL;
    return (int) (state % DUEL_TYPE_PERMUTATIONS.length);
  }

  private String hashHex(String input) {
    String hex = Integer.toHexString(input.hashCode());
    if (hex.length() < 8) {
      return ("00000000" + hex).substring(hex.length());
    }
    return hex.substring(0, 8);
  }

  private DuelSessionView toView(DuelSession d, UUID viewerUserId) {
    String displaySeed = d.getSeed();
    PuzzleType displayType = d.getPuzzleType();

    if ((d.getStatus() == DuelStatus.INFILTRATING || d.getStatus() == DuelStatus.PENDING)
        && viewerUserId != null) {
      if (!d.getAttackerUserId().equals(viewerUserId)) {
        displaySeed = "";
        displayType = PuzzleType.SEQUENCE_INPUT;
      } else if (d.getStatus() == DuelStatus.PENDING) {
        displaySeed = "";
        displayType = PuzzleType.SEQUENCE_INPUT;
      }
    } else if (d.getStatus() == DuelStatus.ACTIVE && viewerUserId != null) {
      boolean isAttacker = d.getAttackerUserId().equals(viewerUserId);
      boolean isDefender = d.getDefenderUserId().equals(viewerUserId);
      if (isAttacker || isDefender) {
        int completed = isAttacker ? d.getAttackerRoundsWon() : d.getDefenderRoundsWon();
        if (completed < DUEL_PUZZLES_REQUIRED) {
          int puzzleIndex = completed + 1;
          String side = isAttacker ? VILLAIN_SIDE : HERO_SIDE;
          displaySeed = duelSeedForPlayer(d.getSeed(), side, puzzleIndex);
          displayType = duelPuzzleTypeForPlayer(d.getSeed(), side, puzzleIndex);
        }
      }
    }

    return new DuelSessionView(
        d.getId(),
        d.getMissionId(),
        d.getAttackerUserId(),
        d.getDefenderUserId(),
        displayName(d.getAttackerUserId()),
        displayName(d.getDefenderUserId()),
        displaySeed,
        displayType,
        d.getStatus(),
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
    static DuelWsMessage pendingHeroAccept(Instant acceptUntil) {
      return new DuelWsMessage(
          "duel:pending-hero-accept",
          java.util.Map.of("acceptUntil", acceptUntil.toString()));
    }

    static DuelWsMessage started(String seed, String puzzleType, int roundMax, Instant timeoutAt) {
      return new DuelWsMessage("duel:started",
          java.util.Map.of("seed", seed, "puzzleType", puzzleType, "roundMax", roundMax, "timeoutAt", timeoutAt.toString()));
    }
    static DuelWsMessage scoreUpdate(int villainScore, int heroScore) {
      return new DuelWsMessage(
          "duel:score-update",
          java.util.Map.of("villainScore", villainScore, "heroScore", heroScore));
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
