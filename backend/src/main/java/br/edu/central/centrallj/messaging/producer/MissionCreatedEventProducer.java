package br.edu.central.centrallj.messaging.producer;

import br.edu.central.centrallj.messaging.event.MissionCreatedKafkaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MissionCreatedEventProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topicCreated;

  public MissionCreatedEventProducer(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${central-lj.kafka.topic-created}") String topicCreated) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topicCreated = topicCreated;
  }

  public void publish(MissionCreatedKafkaEvent event) {
    String key = event.missionId().toString();
    kafkaTemplate.send(topicCreated, key, toJson(event));
  }

  private String toJson(MissionCreatedKafkaEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar MissionCreatedKafkaEvent", e);
    }
  }
}
