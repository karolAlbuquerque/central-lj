package br.edu.central.centrallj;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.central.centrallj.messaging.producer.MissionCreatedEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HeroEquipeAssignmentApiIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private MissionCreatedEventProducer missionCreatedEventProducer;

  @Test
  void criarEquipeValida201() throws Exception {
    String body =
        objectMapper.writeValueAsString(
            Map.of("nome", "Titans", "especialidadePrincipal", "Metahumanos", "ativa", true));
    mockMvc
        .perform(post("/api/teams").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.nome").value("Titans"));
  }

  @Test
  void criarHeroiValido201() throws Exception {
    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "nomeHeroico",
                "Asa Noturna",
                "especialidade",
                "Investigação urbana",
                "statusDisponibilidade",
                "DISPONIVEL",
                "nivel",
                "A",
                "ativo",
                true));

    mockMvc
        .perform(post("/api/heroes").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.nomeHeroico").value("Asa Noturna"));
  }

  @Test
  void atribuirHeroiAMissaoRegistraHistoricoDetalheEMissoesDoHeroi() throws Exception {
    String teamBody =
        objectMapper.writeValueAsString(
            Map.of("nome", "Outsiders", "especialidadePrincipal", "Operações", "ativa", true));
    String teamRes =
        mockMvc
            .perform(post("/api/teams").contentType(MediaType.APPLICATION_JSON).content(teamBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String equipeId = objectMapper.readTree(teamRes).get("id").asText();

    String heroBody =
        objectMapper.writeValueAsString(
            Map.of(
                "nomeHeroico",
                "Batgirl",
                "especialidade",
                "Tecnologia",
                "statusDisponibilidade",
                "DISPONIVEL",
                "nivel",
                "B",
                "ativo",
                true,
                "equipeId",
                equipeId));

    String heroRes =
        mockMvc
            .perform(post("/api/heroes").contentType(MediaType.APPLICATION_JSON).content(heroBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String heroiId = objectMapper.readTree(heroRes).get("id").asText();

    String missionBody =
        objectMapper.writeValueAsString(
            Map.of(
                "titulo",
                "Patrulha Gotham",
                "tipoAmeaca",
                "TECNOLOGICA",
                "prioridade",
                "MEDIA"));
    String missionRes =
        mockMvc
            .perform(
                post("/api/missions").contentType(MediaType.APPLICATION_JSON).content(missionBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String missionId = objectMapper.readTree(missionRes).get("id").asText();
    verify(missionCreatedEventProducer, times(1)).publish(any());

    String assignBody =
        objectMapper.writeValueAsString(Map.of("heroiId", heroiId, "atribuidoPor", "Coordenação"));

    mockMvc
        .perform(
            patch("/api/missions/" + missionId + "/assign-hero")
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.atribuicao.heroiId").value(heroiId))
        .andExpect(jsonPath("$.atribuicao.nomeHeroico").value("Batgirl"))
        .andExpect(jsonPath("$.atribuicao.equipeId").value(nullValue()));

    mockMvc
        .perform(get("/api/missions/" + missionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.missao.atribuicao.heroiId").value(heroiId))
        .andExpect(jsonPath("$.missao.atribuicao.nomeHeroico").value("Batgirl"))
        .andExpect(jsonPath("$.historico[1].origem").value("API_ATRIBUICAO"))
        .andExpect(jsonPath("$.historico[1].mensagem").value(containsString("Batgirl")));

    mockMvc
        .perform(get("/api/heroes/" + heroiId + "/missions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(missionId));
  }

  @Test
  void atribuirEquipeAMissaoRegistraHistoricoEDetalhe() throws Exception {
    String teamBody =
        objectMapper.writeValueAsString(
            Map.of("nome", "Liga Júnior", "especialidadePrincipal", "Resgate", "ativa", true));
    String teamRes =
        mockMvc
            .perform(post("/api/teams").contentType(MediaType.APPLICATION_JSON).content(teamBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String equipeId = objectMapper.readTree(teamRes).get("id").asText();

    String missionBody =
        objectMapper.writeValueAsString(
            Map.of(
                "titulo",
                "Evacuação costeira",
                "tipoAmeaca",
                "DESASTRE_NATURAL",
                "prioridade",
                "ALTA"));

    String missionRes =
        mockMvc
            .perform(
                post("/api/missions").contentType(MediaType.APPLICATION_JSON).content(missionBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String missionId = objectMapper.readTree(missionRes).get("id").asText();

    String assignBody =
        objectMapper.writeValueAsString(Map.of("equipeId", equipeId, "atribuidoPor", "Aquaman"));

    mockMvc
        .perform(
            patch("/api/missions/" + missionId + "/assign-team")
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.atribuicao.equipeId").value(equipeId))
        .andExpect(jsonPath("$.atribuicao.nomeEquipe").value("Liga Júnior"))
        .andExpect(jsonPath("$.atribuicao.heroiId").value(nullValue()));

    mockMvc
        .perform(get("/api/missions/" + missionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.missao.atribuicao.equipeId").value(equipeId))
        .andExpect(jsonPath("$.missao.atribuicao.nomeEquipe").value("Liga Júnior"))
        .andExpect(jsonPath("$.historico[1].origem").value("API_ATRIBUICAO"))
        .andExpect(jsonPath("$.historico[1].mensagem").value(containsString("Liga Júnior")));
  }
}
