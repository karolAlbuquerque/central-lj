package br.edu.central.centrallj.application.model;

import br.edu.central.centrallj.domain.MissionInviteStatus;
import br.edu.central.centrallj.domain.MissionMemberRole;
import java.time.Instant;
import java.util.UUID;

public record MissionMemberView(
    UUID id,
    UUID missionId,
    UUID userId,
    String userName,
    MissionMemberRole role,
    MissionInviteStatus inviteStatus,
    Instant joinedAt) {}
