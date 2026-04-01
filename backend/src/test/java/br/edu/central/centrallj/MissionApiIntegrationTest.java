package br.edu.central.centrallj;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.central.centrallj.messaging.producer.MissionCreatedEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MissionApiIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private MissionCreatedEventProducer missionCreatedEventProducer;

  @Test
  void healthOk() throws Exception {
    mockMvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").exists());
  }

  @Test
  void postMissaoValidaCriaRecebidaEPublicaEvento() throws Exception {
    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "titulo", "Teste integração",
                "descricao", "Fluxo API",
                "tipoAmeaca", "TECNOLOGICA",
                "prioridade", "MEDIA"));

    String res =
        mockMvc
            .perform(
                post("/api/missions").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("RECEBIDA"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = objectMapper.readTree(res).get("id").asText();
    verify(missionCreatedEventProducer, times(1)).publish(any());

    mockMvc
        .perform(get("/api/missions/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.missao.status").value("RECEBIDA"))
        .andExpect(jsonPath("$.historico").isArray())
        .andExpect(jsonPath("$.historico[0].statusNovo").value("RECEBIDA"));
  }

  @Test
  void dashboardSummaryResponde() throws Exception {
    mockMvc.perform(get("/api/missions/dashboard/summary")).andExpect(status().isOk());
  }

  @Test
  void detalheInexistente404() throws Exception {
    mockMvc
        .perform(get("/api/missions/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }
}
