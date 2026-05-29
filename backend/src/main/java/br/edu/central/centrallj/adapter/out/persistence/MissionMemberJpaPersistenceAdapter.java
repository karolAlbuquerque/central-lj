package br.edu.central.centrallj.adapter.out.persistence;

import br.edu.central.centrallj.adapter.out.persistence.entity.MissionMemberEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.PlayerMissionEntity;
import br.edu.central.centrallj.adapter.out.persistence.entity.UsuarioEntity;
import br.edu.central.centrallj.adapter.out.persistence.mapper.PvpPersistenceMapper;
import br.edu.central.centrallj.adapter.out.persistence.repository.MissionMemberRepository;
import br.edu.central.centrallj.adapter.out.persistence.repository.PlayerMissionRepository;
import br.edu.central.centrallj.adapter.out.persistence.repository.UsuarioRepository;
import br.edu.central.centrallj.application.exception.ResourceNotFoundException;
import br.edu.central.centrallj.application.port.out.MissionMemberPersistencePort;
import br.edu.central.centrallj.domain.MissionMember;
import br.edu.central.centrallj.domain.MissionInviteStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MissionMemberJpaPersistenceAdapter implements MissionMemberPersistencePort {

  private final MissionMemberRepository memberRepository;
  private final PlayerMissionRepository missionRepository;
  private final UsuarioRepository usuarioRepository;
  private final PvpPersistenceMapper mapper;

  public MissionMemberJpaPersistenceAdapter(
      MissionMemberRepository memberRepository,
      PlayerMissionRepository missionRepository,
      UsuarioRepository usuarioRepository,
      PvpPersistenceMapper mapper) {
    this.memberRepository = memberRepository;
    this.missionRepository = missionRepository;
    this.usuarioRepository = usuarioRepository;
    this.mapper = mapper;
  }

  @Override
  public MissionMember save(MissionMember member) {
    PlayerMissionEntity missionEntity =
        missionRepository
            .findById(member.getMissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada."));
    UsuarioEntity userEntity =
        usuarioRepository
            .findById(member.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

    MissionMemberEntity entity = new MissionMemberEntity();
    entity.setId(member.getId());
    entity.setMission(missionEntity);
    entity.setUser(userEntity);
    entity.setRole(member.getRole());
    entity.setInviteStatus(member.getInviteStatus());
    entity.setJoinedAt(member.getJoinedAt());
    entity.setInvitedByUserId(member.getInvitedByUserId());

    return mapper.toDomain(memberRepository.save(entity));
  }

  @Override
  public List<MissionMember> findByMission(UUID missionId) {
    return memberRepository.findByMissionId(missionId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<MissionMember> findByMissionAndInviteStatus(UUID missionId, MissionInviteStatus inviteStatus) {
    return memberRepository.findByMissionIdAndInviteStatus(missionId, inviteStatus).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<MissionMember> findByUser(UUID userId) {
    return memberRepository.findByUserId(userId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public long countByMissionAndInviteStatus(UUID missionId, MissionInviteStatus inviteStatus) {
    return memberRepository.countByMissionIdAndInviteStatus(missionId, inviteStatus);
  }

  @Override
  public Optional<MissionMember> findByMissionAndUser(UUID missionId, UUID userId) {
    return memberRepository.findByMissionIdAndUserId(missionId, userId).map(mapper::toDomain);
  }

  @Override
  public boolean existsByMissionAndUser(UUID missionId, UUID userId) {
    return memberRepository.existsByMissionIdAndUserId(missionId, userId);
  }

  @Override
  public void delete(UUID memberId) {
    memberRepository.deleteById(memberId);
  }
}
