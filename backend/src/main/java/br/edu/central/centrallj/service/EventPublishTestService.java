package br.edu.central.centrallj.service;

import br.edu.central.centrallj.dto.EventPublishTestRequest;
import br.edu.central.centrallj.messaging.event.MissionEvent;
import br.edu.central.centrallj.messaging.producer.MissionEventProducer;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EventPublishTestService {

  private final MissionEventProducer producer;

  public EventPublishTestService(MissionEventProducer producer) {
    this.producer = producer;
  }

  public MissionEvent publishTest(EventPublishTestRequest request) {
    String titulo =
        request.mensagem() != null && !request.mensagem().isBlank()
            ? request.mensagem()
            : "Evento de teste N1 — Central-LJ";

    MissionEvent event =
        new MissionEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "InfrastructureTest",
            "RECEBIDA",
            Instant.now(),
            titulo);

    producer.publish(event);
    return event;
  }
}
