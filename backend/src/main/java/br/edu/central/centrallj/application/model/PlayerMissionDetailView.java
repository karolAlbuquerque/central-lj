package br.edu.central.centrallj.application.model;

import java.util.List;

public record PlayerMissionDetailView(
    PlayerMissionView mission,
    List<MissionMemberView> members,
    List<MissionTaskView> tasks) {}
