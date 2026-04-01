package br.edu.central.centrallj.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer mínimo (N1): comprova que o broker entrega mensagens ao backend. O processamento real de
 * missão virá em etapas futuras.
 */
@Component
public class MissionEventsConsumer {

  private static final Logger log = LoggerFactory.getLogger(MissionEventsConsumer.class);

  @KafkaListener(
      id = "centralLjMissionEventsN1",
      topics = "${central-lj.kafka.topic}",
      groupId = "${spring.kafka.consumer.group-id}",
      autoStartup = "${central-lj.kafka.consumer-enabled:true}")
  public void onMissionEvent(String payload) {
    log.info("[Central-LJ][Kafka] Evento recebido: {}", payload);
  }
}
