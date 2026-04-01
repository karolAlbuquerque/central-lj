package br.edu.central.centrallj.service;

import br.edu.central.centrallj.dto.MissionTestRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MissionTestService {

  /**
   * N1: apenas confirma o recebimento do payload (sem publicar no Kafka). A mensageria é exercitada
   * em {@code /api/events/publish-test}.
   */
  public Map<String, Object> acceptTestMission(MissionTestRequest request) {
    String protocolo = UUID.randomUUID().toString();
    Map<String, Object> recebido = new LinkedHashMap<>();
    recebido.put("titulo", request.titulo());
    recebido.put("descricao", request.descricao());
    recebido.put("tipoAmeaca", request.tipoAmeaca());
    recebido.put("prioridade", request.prioridade());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("ok", true);
    body.put("message", "Missão de teste recebida pela Central-LJ.");
    body.put("protocolo", protocolo);
    body.put("recebido", recebido);
    body.put("timestamp", Instant.now().toString());
    return body;
  }
}
