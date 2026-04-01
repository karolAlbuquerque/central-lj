package br.edu.central.centrallj.controller;

import br.edu.central.centrallj.dto.EventPublishTestRequest;
import br.edu.central.centrallj.messaging.event.MissionEvent;
import br.edu.central.centrallj.service.EventPublishTestService;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventTestController {

  private final EventPublishTestService eventPublishTestService;

  public EventTestController(EventPublishTestService eventPublishTestService) {
    this.eventPublishTestService = eventPublishTestService;
  }

  @PostMapping("/publish-test")
  public ResponseEntity<?> publishTest(@RequestBody(required = false) EventPublishTestRequest request) {
    try {
      EventPublishTestRequest body = request != null ? request : new EventPublishTestRequest(null);
      MissionEvent event = eventPublishTestService.publishTest(body);
      return ResponseEntity.ok(
          Map.of(
              "ok", true,
              "message",
                  "Evento publicado no Kafka. O consumer da aplicação registra a mensagem nos logs.",
              "event", event,
              "timestamp", Instant.now().toString()));
    } catch (KafkaException ex) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(
              Map.of(
                  "ok", false,
                  "message",
                      "Kafka indisponível. Suba a infra (infra/docker-compose.yml) e confira localhost:9092.",
                  "timestamp", Instant.now().toString()));
    }
  }
}
