package br.edu.central.centrallj.messaging.producer;

import br.edu.central.centrallj.messaging.event.MissionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MissionEventProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topic;

  public MissionEventProducer(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${central-lj.kafka.topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topic = topic;
  }

  public void publish(MissionEvent event) {
    String key = event.missionId() != null ? event.missionId().toString() : UUID.randomUUID().toString();
    kafkaTemplate.send(topic, key, toJson(event));
  }

  private String toJson(MissionEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar MissionEvent para JSON", e);
    }
  }
}

