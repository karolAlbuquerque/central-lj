# N2 — Decisões técnicas (fechamento)

## Histórico da missão

- Tabela **`mission_history`** (Flyway `V2__mission_history.sql`): `mission_id`, `status_anterior` (nullable no primeiro registro da API), `status_novo`, `mensagem`, `origem` (`API_REGISTRO` | `KAFKA_WORKFLOW` | `KAFKA_WORKFLOW_ERRO`), `ocorrido_em`.
- **`MissionHistoryRecorder`** centraliza inserts; chamado por **`MissionCommandService`** (abertura) e **`MissionWorkflowService`** (transições e falha).

## Mensageria desacoplada

| Peça | Responsabilidade |
|------|------------------|
| **`MissionCreatedEventProducer`** | Serializa e envia string JSON ao tópico configurado. |
| **`AfterCommitMissionDispatch`** | Garante publish + SSE **após commit** da criação (evita inconsistência). |
| **`MissionCreatedConsumer`** | Adaptador Kafka → aplicação (sem regra de domínio). |
| **`MissionCreatedEventIngestionService`** | Parse JSON, validação de `type` / `missionId`, delega ao workflow. |
| **`MissionEventsConsumer`** | Apenas **`missions.events`** — log/debug; **não** participa do workflow. |

Contrato publicado: record **`MissionCreatedKafkaEvent`**; fábrica **`MissionCreatedEventFactory`** mantém o tipo **`MISSION_CREATED`** estável.

## Tempo real (SSE)

- **`GET /api/missions/stream`** — `SseEmitter`, evento **`mission-update`** com `missionId`.
- **`MissionRealtimeNotifier`** — lista thread-safe de emitters.
- Motivos: canal **servidor → cliente** suficiente; menos estado que WebSocket; combina com proxy Vite.
- Frontend: **SSE + polling 12s** como redundância.

## Dashboard e consultas

- **`GET /api/missions/dashboard/summary`**: agregados + `findTop12ByOrderByDataCriacaoDesc`.
- **`GET /api/missions/recent`**, **`GET /api/missions/status/{status}`**, **`GET /api/missions/{id}/history`**: apoio à demo e integrações.

## Strategy

- **`MissionProcessingFlowStrategyResolver`**: **CRÍTICA** ↔ fluxo encurtado; demais prioridades ↔ fluxo padrão.

## Testes

- Perfil **`test`**: H2, Flyway off, `workflow-step-delay-ms: 0`, consumer Kafka off.
- **`@MockitoBean`** no **`MissionCreatedEventProducer`** nos testes de API que não precisam de broker (Spring Boot 3.4+).

## Porta PostgreSQL (Compose)

- Host **5433** no `README` para evitar conflito com Postgres local na **5432**.

## Cobertura JaCoCo

- Plugin configurado no `pom.xml`; relatório HTML em `target/site/jacoco/` após `.\mvnw.cmd test` (Windows) ou `./mvnw test`.
