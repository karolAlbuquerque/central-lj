package br.edu.central.centrallj.adapter.in.web.dto;

import br.edu.central.centrallj.domain.MissionInviteStatus;
import br.edu.central.centrallj.domain.MissionMemberRole;
import java.time.Instant;
import java.util.UUID;

public record MissionMemberResponse(
    UUID id,
    UUID missionId,
    UUID userId,
    String userName,
    MissionMemberRole role,
    MissionInviteStatus inviteStatus,
    Instant joinedAt) {}
