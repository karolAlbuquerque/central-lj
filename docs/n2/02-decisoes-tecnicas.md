# N2 — Decisões técnicas (atualizado)

## Histórico da missão

- Tabela **`mission_history`** (Flyway `V2__mission_history.sql`): `mission_id`, `status_anterior` (nullable no primeiro registro da API), `status_novo`, `mensagem`, `origem` (enum `API_REGISTRO` | `KAFKA_WORKFLOW` | `KAFKA_WORKFLOW_ERRO`), `ocorrido_em`.
- **MissionHistoryRecorder** centraliza inserts; chamado a partir de **`MissionCommandService`** (abertura) e **`MissionWorkflowService`** (transições e falha).
- **GET /api/missions/{id}** retorna **detalhe + lista cronológica** para a timeline na UI.

## Tempo real (SSE)

- **`GET /api/missions/stream`** com **`text/event-stream`** e **`SseEmitter`**.
- **MissionRealtimeNotifier** mantém lista thread-safe de emitters; envia evento **`mission-update`** com JSON `{"missionId":"..."}` após commit da API e após cada passo do workflow.
- Motivos da escolha: um canal **servidor → cliente** basta; menos estado que WebSocket; adequado atrás do **proxy Vite** (`timeout: 0`).
- O front combina **SSE + polling 12s** para robustez se o stream cair ou em redes restritivas.

## Dashboard

- **`GET /api/missions/dashboard/summary`**: totais via `count` / `countByStatusIn` (em andamento = soma de EM_ANALISE, PRIORIZADA, EQUIPE_DESIGNADA, EM_ANDAMENTO) + `findTop12ByOrderByDataCriacaoDesc`.
- Evita múltiplos round-trips no primeiro paint do painel.

## Strategy (existente)

- Mantida **MissionProcessingFlowStrategy** para fluxo normal vs **CRÍTICA**; apenas ora cada passo gera **histórico** notificável.

## Testes

- **H2** + perfil **`test`**: `workflow-step-delay-ms: 0` para integração rápida.
- **@MockBean** `MissionCreatedEventProducer` em testes de API que não precisam de broker real.

## Porta PostgreSQL (Compose)

- Host **5433** documentada no README raiz para evitar conflito com Postgres local na 5432.
