package br.edu.central.centrallj.adapter.in.web.dto;

import java.util.List;

public record PlayerMissionDetailResponse(
    PlayerMissionResponse mission,
    List<MissionMemberResponse> members,
    List<MissionTaskResponse> tasks) {}
