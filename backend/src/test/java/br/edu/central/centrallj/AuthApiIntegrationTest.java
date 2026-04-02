package br.edu.central.centrallj;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.central.centrallj.bootstrap.DemoAuthDataLoader;
import br.edu.central.centrallj.messaging.producer.MissionCreatedEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {"central-lj.security.enabled=true", "central-lj.auth.demo-seed=true"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private MissionCreatedEventProducer missionCreatedEventProducer;

  @Test
  void loginCredenciaisInvalidas401() throws Exception {
    String body =
        objectMapper.writeValueAsString(
            Map.of("email", "naoexiste@x.demo", "password", "errada"));
    mockMvc
        .perform(
            post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginAdminEMe() throws Exception {
    String loginBody =
        objectMapper.writeValueAsString(
            Map.of("email", "coordenacao@central-lj.demo", "password", "Admin@demo2026"));

    String res =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.user.role").value("ADMIN"))
            .andExpect(jsonPath("$.user.heroiId").value(nullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String token = objectMapper.readTree(res).get("accessToken").asText();

    mockMvc
        .perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("coordenacao@central-lj.demo"))
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  void heroiMinhasMissoesEAtribuicao() throws Exception {
    String adminLogin =
        objectMapper.writeValueAsString(
            Map.of("email", "coordenacao@central-lj.demo", "password", "Admin@demo2026"));
    String adminRes =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(adminLogin))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String adminToken = objectMapper.readTree(adminRes).get("accessToken").asText();

    String missionBody =
        objectMapper.writeValueAsString(
            Map.of(
                "titulo",
                "Missão do Guardião Demo",
                "tipoAmeaca",
                "TECNOLOGICA",
                "prioridade",
                "MEDIA"));

    String missionRes =
        mockMvc
            .perform(
                post("/api/missions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(missionBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String missionId = objectMapper.readTree(missionRes).get("id").asText();
    verify(missionCreatedEventProducer).publish(any());

    String assignBody =
        objectMapper.writeValueAsString(
            Map.of("heroiId", DemoAuthDataLoader.DEMO_HEROI_ID.toString(), "atribuidoPor", "Admin"));

    mockMvc
        .perform(
            patch("/api/missions/" + missionId + "/assign-hero")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignBody))
        .andExpect(status().isOk());

    String heroLogin =
        objectMapper.writeValueAsString(
            Map.of("email", "heroi.demo@central-lj.demo", "password", "Hero@demo2026"));
    String heroRes =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(heroLogin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value("HERO"))
            .andExpect(jsonPath("$.user.heroiId").value(DemoAuthDataLoader.DEMO_HEROI_ID.toString()))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String heroToken = objectMapper.readTree(heroRes).get("accessToken").asText();

    mockMvc
        .perform(get("/api/me/missions").header(HttpHeaders.AUTHORIZATION, "Bearer " + heroToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(missionId))
        .andExpect(jsonPath("$[0].titulo").value("Missão do Guardião Demo"));

    mockMvc
        .perform(
            get("/api/missions/" + missionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + heroToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.missao.id").value(missionId));

    mockMvc
        .perform(get("/api/missions").header(HttpHeaders.AUTHORIZATION, "Bearer " + heroToken))
        .andExpect(status().isForbidden());
  }
}
