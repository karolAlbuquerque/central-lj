# N2 — Testes automatizados

## O que existe hoje

| Suíte | Objetivo |
|-------|-----------|
| `SmokeTest` | Sobe o contexto Spring no perfil `test` (H2, Kafka consumer off). |
| `MissionApiIntegrationTest` | **MockMvc**: health; **POST /api/missions** com corpo válido (producer Kafka **mockado**); verifica detalhe com **histórico**; **GET /dashboard/summary**; **404** em id inexistente. |
| `MissionWorkflowIntegrationTest` | **Sem** Kafka: insere missão **RECEBIDA** no repositório, roda `MissionWorkflowService.processAfterCreation`; assert **CONCLUIDA** e **5** linhas de histórico; fluxo **CRÍTICA** (primeiro passo **PRIORIZADA**); **`markFailure`** grava **FALHA** + histórico. |

## Tipos de teste

- **Integração (Spring Boot):** API + JPA + transações reais em H2.
- **Unitário leve:** pré-requisitos de domínio cobertos pelos testes acima; mocks apenas do **producer** Kafka onde o broker não é necessário.

## Como executar

Na pasta `backend/`:

```bash
mvn test
```

Perfil activo: `test` (`src/test/resources/application-test.yml`) — Flyway **desligado**, Hibernate **`create-drop`**, **`workflow-step-delay-ms: 0`**.

## O que falta (evolução)

- **Testcontainers** (Postgres + opcional Kafka) no CI.
- Teste **E2E** (Playwright) do fluxo painel → criar → ver timeline.
- Cobertura de **SSE** (opcional, mais frágil em CI).
