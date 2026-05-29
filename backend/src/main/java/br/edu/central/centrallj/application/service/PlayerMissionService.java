package br.edu.central.centrallj.application.service;

import br.edu.central.centrallj.application.exception.BadRequestException;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.model.AssignTaskCommand;
import br.edu.central.centrallj.application.model.CreatePlayerMissionCommand;
import br.edu.central.centrallj.application.model.ExecuteTaskCommand;
import br.edu.central.centrallj.application.model.MissionMemberView;
import br.edu.central.centrallj.application.model.MissionTaskView;
import br.edu.central.centrallj.application.model.PlayerMissionDetailView;
import br.edu.central.centrallj.application.model.PlayerMissionView;
import br.edu.central.centrallj.application.port.in.AssignMissionTaskUseCase;
import br.edu.central.centrallj.application.port.in.CloseMissionUseCase;
import br.edu.central.centrallj.application.port.in.CreatePlayerMissionUseCase;
import br.edu.central.centrallj.application.port.in.ExecuteMissionTaskUseCase;
import br.edu.central.centrallj.application.port.in.GetPlayerMissionUseCase;
import br.edu.central.centrallj.application.port.in.InviteMissionMemberUseCase;
import br.edu.central.centrallj.application.port.out.MissionMemberPersistencePort;
import br.edu.central.centrallj.application.port.out.MissionTaskPersistencePort;
import br.edu.central.centrallj.application.port.out.PlayerMissionPersistencePort;
import br.edu.central.centrallj.application.port.out.UsuarioPersistencePort;
import br.edu.central.centrallj.domain.MissionCombatState;
import br.edu.central.centrallj.domain.MissionInviteStatus;
import br.edu.central.centrallj.domain.MissionMember;
import br.edu.central.centrallj.domain.MissionMemberRole;
import br.edu.central.centrallj.domain.MissionTask;
import br.edu.central.centrallj.domain.PlayerMission;
import br.edu.central.centrallj.domain.PuzzleType;
import br.edu.central.centrallj.domain.TaskStatus;
import br.edu.central.centrallj.domain.UserRole;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerMissionService
    implements CreatePlayerMissionUseCase,
        GetPlayerMissionUseCase,
        InviteMissionMemberUseCase,
        AssignMissionTaskUseCase,
        ExecuteMissionTaskUseCase,
        CloseMissionUseCase {

  private final PlayerMissionPersistencePort missionPort;
  private final MissionMemberPersistencePort memberPort;
  private final MissionTaskPersistencePort taskPort;
  private final UsuarioPersistencePort usuarioPort;
  private final PuzzleValidationService puzzleValidation;

  public PlayerMissionService(
      PlayerMissionPersistencePort missionPort,
      MissionMemberPersistencePort memberPort,
      MissionTaskPersistencePort taskPort,
      UsuarioPersistencePort usuarioPort,
      PuzzleValidationService puzzleValidation) {
    this.missionPort = missionPort;
    this.memberPort = memberPort;
    this.taskPort = taskPort;
    this.usuarioPort = usuarioPort;
    this.puzzleValidation = puzzleValidation;
  }

  @Override
  @Transactional
  public PlayerMissionView create(CreatePlayerMissionCommand command) {
    var user =
        usuarioPort
            .findById(command.ownerUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    if (user.getRole() != UserRole.HERO) {
      throw new BadRequestException("Apenas heróis podem criar missões.");
    }

    Instant now = Instant.now();
    PlayerMission mission = new PlayerMission();
    mission.setId(UUID.randomUUID());
    mission.setTitulo(command.titulo().trim());
    mission.setDescricao(command.descricao() == null || command.descricao().isBlank() ? null : command.descricao().trim());
    mission.setOwnerUserId(command.ownerUserId());
    mission.setCombatState(MissionCombatState.LOBBY);
    mission.setMinPlayers(command.minPlayers() != null && command.minPlayers() > 0 ? command.minPlayers() : 1);
    mission.setPartySeed(savedPartySeed(savedUuid(now, command.ownerUserId())));
    mission.setStartedByUserId(null);
    mission.setStartedAt(null);
    mission.setCreatedAt(now);
    mission.setUpdatedAt(now);

    PlayerMission saved = missionPort.save(mission);

    MissionMember chefe = new MissionMember();
    chefe.setId(UUID.randomUUID());
    chefe.setMissionId(saved.getId());
    chefe.setUserId(command.ownerUserId());
    chefe.setRole(MissionMemberRole.CHEFE);
    chefe.setInviteStatus(MissionInviteStatus.ACCEPTED);
    chefe.setJoinedAt(now);
    memberPort.save(chefe);

    return toView(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public PlayerMissionDetailView getDetail(UUID missionId, UUID requestingUserId) {
    PlayerMission mission =
        missionPort.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    assertIsMember(missionId, requestingUserId);
    List<MissionMemberView> members =
        memberPort.findByMission(missionId).stream().map(this::toMemberView).toList();
    UUID ownerUserId = mission.getOwnerUserId();
    List<MissionTaskView> tasks =
        taskPort.findByMission(missionId).stream()
            .map(t -> ensureCriticalPuzzleType(t))
            .map(t -> toTaskView(t, ownerUserId, requestingUserId))
            .toList();
    return new PlayerMissionDetailView(toView(mission), members, tasks);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlayerMissionView> listByOwner(UUID ownerUserId) {
    return missionPort.findByOwner(ownerUserId).stream().map(this::toView).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlayerMissionView> listForMember(UUID userId) {
    return memberPort.findByUser(userId).stream()
        .map(MissionMember::getMissionId)
        .distinct()
        .map(
            missionId ->
                missionPort
                    .findById(missionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada.")))
        .map(this::toView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlayerMissionView> listAll() {
    return missionPort.findAll().stream().map(this::toView).toList();
  }

  @Override
  @Transactional
  public MissionMemberView invite(UUID missionId, UUID invitedUserId, UUID requestingUserId) {
    assertIsChefe(missionId, requestingUserId);
    var invited =
        usuarioPort
            .findById(invitedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário convidado não encontrado."));
    if (invited.getRole() != UserRole.HERO) {
      throw new BadRequestException("Apenas heróis podem ser convidados para missões.");
    }
    if (memberPort.existsByMissionAndUser(missionId, invitedUserId)) {
      throw new BadRequestException("Usuário já é membro desta missão.");
    }

    MissionMember member = new MissionMember();
    member.setId(UUID.randomUUID());
    member.setMissionId(missionId);
    member.setUserId(invitedUserId);
    member.setRole(MissionMemberRole.HERO_MEMBER);
    member.setInviteStatus(MissionInviteStatus.PENDING);
    member.setJoinedAt(Instant.now());
    member.setInvitedByUserId(requestingUserId);

    return toMemberView(memberPort.save(member));
  }

  @Override
  @Transactional
  public MissionMemberView acceptInvite(UUID missionId, UUID memberUserId) {
    MissionMember member =
        memberPort
            .findByMissionAndUser(missionId, memberUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado."));
    if (member.getInviteStatus() == MissionInviteStatus.DECLINED) {
      throw new BadRequestException("Convite já recusado.");
    }
    member.setInviteStatus(MissionInviteStatus.ACCEPTED);
    member.setJoinedAt(Instant.now());
    MissionMember saved = memberPort.save(member);

    PlayerMission mission =
        missionPort.findById(missionId).orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    if (mission.getCombatState() == MissionCombatState.LOBBY) {
      long accepted = memberPort.countByMissionAndInviteStatus(missionId, MissionInviteStatus.ACCEPTED);
      if (accepted >= mission.getMinPlayers()) {
        startMission(missionId, mission.getOwnerUserId(), false);
      }
    }
    return toMemberView(saved);
  }

  @Override
  @Transactional
  public MissionMemberView declineInvite(UUID missionId, UUID memberUserId) {
    MissionMember member =
        memberPort
            .findByMissionAndUser(missionId, memberUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado."));
    if (member.getRole() == MissionMemberRole.CHEFE) {
      throw new BadRequestException("Chefe da missão não pode recusar a própria missão.");
    }
    member.setInviteStatus(MissionInviteStatus.DECLINED);
    return toMemberView(memberPort.save(member));
  }

  @Override
  @Transactional
  public PlayerMissionView startMission(UUID missionId, UUID requestingUserId, boolean forceStart) {
    assertIsChefe(missionId, requestingUserId);
    PlayerMission mission =
        missionPort.findById(missionId).orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    if (mission.getCombatState() != MissionCombatState.LOBBY) {
      throw new BadRequestException("A missão já foi iniciada.");
    }
    long accepted = memberPort.countByMissionAndInviteStatus(missionId, MissionInviteStatus.ACCEPTED);
    if (!forceStart && accepted < mission.getMinPlayers()) {
      throw new BadRequestException("Quantidade mínima de jogadores ainda não atingida.");
    }

    List<MissionMember> acceptedMembers =
        memberPort.findByMissionAndInviteStatus(missionId, MissionInviteStatus.ACCEPTED);
    if (acceptedMembers.isEmpty()) {
      throw new BadRequestException("Nenhum membro aceito para iniciar missão.");
    }

    generateDefaultPuzzlesForParty(mission, acceptedMembers);

    Instant now = Instant.now();
    mission.setCombatState(MissionCombatState.ACTIVE);
    mission.setStartedByUserId(requestingUserId);
    mission.setStartedAt(now);
    mission.setPartySeed(savedPartySeed(savedUuid(now, requestingUserId)));
    mission.setUpdatedAt(now);
    return toView(missionPort.save(mission));
  }

  @Override
  @Transactional
  public MissionTaskView createTask(AssignTaskCommand command) {
    throw new BadRequestException("Tarefas são geradas automaticamente ao iniciar a missão.");
  }

  @Override
  @Transactional
  public MissionTaskView updateTask(AssignTaskCommand command) {
    throw new BadRequestException("Tarefas são geradas automaticamente ao iniciar a missão.");
  }

  @Override
  @Transactional
  public MissionTaskView execute(ExecuteTaskCommand command) {
    PlayerMission mission =
        missionPort
            .findById(command.missionId())
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    assertIsMember(command.missionId(), command.requestingUserId());

    MissionTask task =
        taskPort
            .findById(command.taskId())
            .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada."));
    if (!task.getMissionId().equals(command.missionId())) {
      throw new BadRequestException("Tarefa não pertence a esta missão.");
    }
    if (!canExecuteTask(mission.getOwnerUserId(), task, command.requestingUserId())) {
      throw new BadRequestException("Você não pode executar esta tarefa.");
    }

    Instant now = Instant.now();
    switch (command.action()) {
      case START -> {
        if (task.getStatus() == TaskStatus.BLOCKED) {
          throw new BadRequestException("Tarefa bloqueada.");
        }
        if (task.getStatus() != TaskStatus.PENDING) {
          throw new BadRequestException("Só é possível iniciar tarefas pendentes.");
        }
        assertDependencyDone(task);
        task.setStatus(TaskStatus.IN_PROGRESS);
      }
      case COMPLETE -> {
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
          throw new BadRequestException("Inicie a tarefa antes de concluir.");
        }
        if (task.isCritical()) {
          task.setStatus(TaskStatus.AWAITING_PUZZLE);
        } else {
          task.setStatus(TaskStatus.DONE);
        }
      }
      case SUBMIT_PUZZLE -> {
        if (task.getStatus() != TaskStatus.AWAITING_PUZZLE) {
          throw new BadRequestException("Esta tarefa não está aguardando puzzle.");
        }
        if (command.moves() == null || command.moves().isEmpty()) {
          throw new BadRequestException("Envie a sequência do puzzle.");
        }
        String seed = taskPuzzleSeed(task.getId());
        PuzzleType puzzleType = task.getPuzzleType() != null ? task.getPuzzleType() : puzzleTypeForTask(task.getId());
        if (!puzzleValidation.validate(seed, puzzleType, command.moves())) {
          throw new BadRequestException("Solução incorreta. Tente novamente.");
        }
        task.setStatus(TaskStatus.DONE);
      }
    }
    task.setUpdatedAt(now);
    return toTaskView(
        taskPort.save(task), mission.getOwnerUserId(), command.requestingUserId());
  }

  @Override
  @Transactional
  public void close(UUID missionId, UUID requestingUserId) {
    assertIsChefe(missionId, requestingUserId);
    PlayerMission mission =
        missionPort.findById(missionId)
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    mission.setCombatState(MissionCombatState.COMPROMETIDA);
    mission.setUpdatedAt(Instant.now());
    missionPort.save(mission);
  }

  private void assertIsChefe(UUID missionId, UUID userId) {
    memberPort
        .findByMissionAndUser(missionId, userId)
        .filter(m -> m.getRole() == MissionMemberRole.CHEFE)
        .orElseThrow(() -> new BadRequestException("Apenas o chefe da missão pode realizar esta operação."));
  }

  private void assertIsMember(UUID missionId, UUID userId) {
    if (!memberPort.existsByMissionAndUser(missionId, userId)) {
      throw new BadRequestException("Acesso restrito a membros da missão.");
    }
  }

  private PlayerMissionView toView(PlayerMission m) {
    return new PlayerMissionView(
        m.getId(), m.getTitulo(), m.getDescricao(),
        m.getOwnerUserId(), m.getCombatState(), m.getMinPlayers(),
        m.getPartySeed(), m.getStartedByUserId(), m.getStartedAt(),
        m.getCreatedAt(), m.getUpdatedAt());
  }

  private MissionMemberView toMemberView(MissionMember m) {
    return new MissionMemberView(
        m.getId(), m.getMissionId(), m.getUserId(),
        m.getUserName(), m.getRole(), m.getInviteStatus(), m.getJoinedAt());
  }

  private void generateDefaultPuzzlesForParty(PlayerMission mission, List<MissionMember> acceptedMembers) {
    Instant now = Instant.now();
    List<MissionTask> generated = new ArrayList<>();
    for (MissionMember member : acceptedMembers) {
      for (int i = 0; i < 3; i++) {
        MissionTask task = new MissionTask();
        task.setId(UUID.randomUUID());
        task.setMissionId(mission.getId());
        task.setAssignedToUserId(member.getUserId());
        task.setTitle("Puzzle " + (i + 1) + " · " + member.getUserName());
        task.setDescription("Gerado automaticamente para a party.");
        task.setStatus(TaskStatus.PENDING);
        task.setCritical(true);
        task.setPuzzleType(puzzleTypeForTask(task.getId()));
        task.setDependsOnTaskId(null);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        generated.add(task);
      }
    }
    for (MissionTask task : generated) {
      taskPort.save(task);
    }
  }

  private static UUID savedUuid(Instant now, UUID owner) {
    return UUID.nameUUIDFromBytes((now.toString() + ":" + owner).getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  private static String savedPartySeed(UUID base) {
    return base.toString().replace("-", "");
  }

  private void assertDependencyDone(MissionTask task) {
    if (task.getDependsOnTaskId() == null) {
      return;
    }
    MissionTask dependency =
        taskPort
            .findById(task.getDependsOnTaskId())
            .orElseThrow(() -> new ResourceNotFoundException("Tarefa dependente não encontrada."));
    if (dependency.getStatus() != TaskStatus.DONE) {
      throw new BadRequestException("Conclua a tarefa dependente antes de iniciar esta.");
    }
  }

  static String taskPuzzleSeed(UUID taskId) {
    return taskId.toString().replace("-", "");
  }

  static PuzzleType puzzleTypeForTask(UUID taskId) {
    String hex = taskPuzzleSeed(taskId).substring(0, 8);
    long state = Long.parseLong(hex, 16) & 0xFFFFFFFFL;
    state = (state * 1664525L + 1013904223L) & 0xFFFFFFFFL;
    PuzzleType[] types = PuzzleType.values();
    return types[(int) (state % types.length)];
  }

  /** Preenche puzzle_type em tarefas críticas antigas criadas antes da coluna existir. */
  private MissionTask ensureCriticalPuzzleType(MissionTask task) {
    if (!task.isCritical() || task.getPuzzleType() != null) {
      return task;
    }
    task.setPuzzleType(puzzleTypeForTask(task.getId()));
    task.setUpdatedAt(Instant.now());
    return taskPort.save(task);
  }

  private boolean canExecuteTask(UUID missionOwnerUserId, MissionTask task, UUID requestingUserId) {
    if (missionOwnerUserId.equals(requestingUserId)) {
      return true;
    }
    UUID assignee = task.getAssignedToUserId();
    return assignee != null && assignee.equals(requestingUserId);
  }

  private MissionTaskView toTaskView(
      MissionTask t, UUID missionOwnerUserId, UUID requestingUserId) {
    return new MissionTaskView(
        t.getId(),
        t.getMissionId(),
        t.getAssignedToUserId(),
        t.getTitle(),
        t.getDescription(),
        t.getStatus(),
        t.isCritical(),
        t.getDependsOnTaskId(),
        t.getCreatedAt(),
        t.getUpdatedAt(),
        canExecuteTask(missionOwnerUserId, t, requestingUserId),
        t.isCritical() ? taskPuzzleSeed(t.getId()) : null,
        t.isCritical()
            ? (t.getPuzzleType() != null ? t.getPuzzleType() : puzzleTypeForTask(t.getId()))
            : null);
  }
}
