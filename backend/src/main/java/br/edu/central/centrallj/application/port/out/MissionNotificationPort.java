package br.edu.central.centrallj.application.port.out;

import java.util.UUID;

public interface MissionNotificationPort {
  void notifyMissionUpdate(UUID missionId);
}
