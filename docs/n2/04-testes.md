# N2 — Testes automatizados (fechamento)

## Suítes

| Suíte | Objetivo |
|-------|----------|
| `SmokeTest` | Sobe o contexto Spring no perfil `test` (H2, consumer Kafka off). |
| `MissionApiIntegrationTest` | **MockMvc:** health; **POST /api/missions** válido (producer mockado **via `@MockitoBean`**); detalhe + histórico (incl. `atribuicao` nula); **dashboard**; **404**; **400** validação; **GET /recent**; **GET /status/{status}**; **400** status inválido; **GET /{id}/history**; **404** histórico inexistente. |
| `HeroEquipeAssignmentApiIntegrationTest` | **MockMvc:** **POST /api/teams** e **POST /api/heroes**; **PATCH** `assign-hero` / `assign-team`; detalhe com **API_ATRIBUICAO** no histórico; **GET /api/heroes/{id}/missions** após designação de herói. |
| `AuthApiIntegrationTest` | **MockMvc** com **`central-lj.security.enabled=true`** e seed demo: login inválido **401**; login admin + **GET /api/auth/me**; fluxo admin cria missão + `assign-hero` para herói demo; login herói + **GET /api/me/missions** + detalhe da missão; herói recebe **403** em **GET /api/missions**. |
| `MissionWorkflowIntegrationTest` | Sem Kafka: `processAfterCreation` até **CONCLUIDA** + histórico; fluxo **CRÍTICA**; **`markFailure`**. |
| `MissionCreatedEventIngestionServiceTest` | Unitário: JSON válido delega workflow; ignora tipo inválido / `missionId` nulo. |
| `MissionProcessingFlowStrategyResolverTest` | Unitário: **CRÍTICA** vs demais prioridades. |
| `AfterCommitMissionDispatchTest` | Unitário: sem transação ativa, publica e notifica imediatamente. |

## Tipos de teste

- **Integração:** API + JPA + transações reais em H2.
- **Unitário:** ingestão Kafka, strategy resolver, dispatch pós-commit.

## Como executar

Na pasta `backend/`:

```powershell
.\mvnw.cmd test
```

(Linux/macOS: `./mvnw test`. Se preferir Maven instalado globalmente: `mvn test`.)

Perfil **`test`** (`application-test.yml`): Flyway **off**, Hibernate **`create-drop`**, **`workflow-step-delay-ms: 0`**, **`central-lj.kafka.consumer-enabled: false`**.

## Cobertura (JaCoCo)

Após os testes:

```text
backend/target/site/jacoco/index.html
```

Útil para **banca** como evidência rápida de cobertura; não é obrigação de meta percentual rígida na N2.

## Evoluções futuras (fora do escopo N2)

- **Testcontainers** (Postgres + Kafka) no CI.
- **E2E** (Playwright) painel → criar → timeline.
- Teste automatizado de **SSE** (mais frágil em CI).
