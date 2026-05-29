package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.entity.MissionTaskEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.PlayerMissionEntity;
import br.edu.central.centrallj.adapter.out.persistence.mapper.PvpPersistenceMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.MissionTaskRepository;
import br.edu.central.centrallj.adapter.out.persistence.repository.PlayerMissionRepository;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.port.out.MissionTaskPersistencePort;
import br.edu.central.centrallj.domain.MissionTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MissionTaskJpaPersistenceAdapter implements MissionTaskPersistencePort {

  private final MissionTaskRepository taskRepository;
  private final PlayerMissionRepository missionRepository;
  private final PvpPersistenceMapper mapper;

  public MissionTaskJpaPersistenceAdapter(
      MissionTaskRepository taskRepository,
      PlayerMissionRepository missionRepository,
      PvpPersistenceMapper mapper) {
    this.taskRepository = taskRepository;
    this.missionRepository = missionRepository;
    this.mapper = mapper;
  }

  @Override
  public MissionTask save(MissionTask task) {
    PlayerMissionEntity missionEntity =
        missionRepository
            .findById(task.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));

    MissionTaskEntity entity = new MissionTaskEntity();
    entity.setId(task.getId());
    entity.setMission(missionEntity);
    entity.setAssignedToUserId(task.getAssignedToUserId());
    entity.setTitle(task.getTitle());
    entity.setDescription(task.getDescription());
    entity.setStatus(task.getStatus());
    entity.setCritical(task.isCritical());
    entity.setPuzzleType(task.getPuzzleType());
    entity.setCreatedAt(task.getCreatedAt());
    entity.setUpdatedAt(task.getUpdatedAt());

    if (task.getDependsOnTaskId() != null) {
      taskRepository
          .findById(task.getDependsOnTaskId())
          .ifPresent(entity::setDependsOnTask);
    }

    return mapper.toDomain(taskRepository.save(entity));
  }

  @Override
  public List<MissionTask> findByMission(UUID missionId) {
    return taskRepository.findByMissionId(missionId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<MissionTask> findById(UUID taskId) {
    return taskRepository.findById(taskId).map(mapper::toDomain);
  }
}
