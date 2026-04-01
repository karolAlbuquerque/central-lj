package br.edu.central.centrallj.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Notificação leve para o front (SSE) quando uma missão muda — complementa o polling de fallback.
 */
@Service
public class MissionRealtimeNotifier {

  private static final Logger log = LoggerFactory.getLogger(MissionRealtimeNotifier.class);

  private final ObjectMapper objectMapper;
  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public MissionRealtimeNotifier(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public SseEmitter subscribe(Duration timeout) {
    SseEmitter emitter = new SseEmitter(timeout.toMillis());
    emitters.add(emitter);
    Runnable remove = () -> emitters.remove(emitter);
    emitter.onCompletion(remove);
    emitter.onTimeout(remove);
    emitter.onError(e -> remove.run());
    try {
      emitter.send(
          SseEmitter.event().name("connected").data(objectMapper.writeValueAsString(Map.of("ok", true))));
    } catch (IOException e) {
      emitter.completeWithError(e);
    }
    return emitter;
  }

  public void notifyMissionUpdate(UUID missionId) {
    if (emitters.isEmpty()) {
      return;
    }
    String json;
    try {
      json = objectMapper.writeValueAsString(Map.of("missionId", missionId.toString()));
    } catch (JsonProcessingException e) {
      log.warn("Falha ao serializar payload SSE", e);
      return;
    }
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("mission-update").data(json));
      } catch (Exception e) {
        emitter.complete();
        emitters.remove(emitter);
      }
    }
  }
}
