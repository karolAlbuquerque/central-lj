# N2 — Fluxo funcional (fechamento)

## Visão do fluxo real

```mermaid
sequenceDiagram
  participant U as Operador (browser)
  participant F as Frontend React
  participant B as Spring Boot API
  participant DB as PostgreSQL
  participant K as Kafka (missions.created)
  participant C as Consumer Spring

  U->>F: Painel / detalhe / nova missão
  F->>B: POST /api/missions
  B->>DB: INSERT missão + INSERT mission_history (RECEBIDA)
  B->>K: MISSION_CREATED (após commit)
  B-->>F: 201 + missao; SSE notify
  K->>C: evento
  loop Pipeline Strategy
    C->>DB: UPDATE status + INSERT mission_history
    B-->>F: SSE mission-update
  end
  F->>B: GET dashboard / detalhe / histórico
```

1. **Criação:** `POST /api/missions` persiste **missão** (`RECEBIDA`) e primeira linha em **`mission_history`** (origem `API_REGISTRO`).
2. **Kafka:** após **commit** da transação, **`AfterCommitMissionDispatch`** publica **`MISSION_CREATED`** em **`missions.created`** e chama **`MissionRealtimeNotifier`** (SSE).
3. **Consumer:** `MissionCreatedConsumer` delega a **`MissionCreatedEventIngestionService`** (parse + validação do envelope); em seguida **`MissionWorkflowService`** aplica a **Strategy**, grava cada transição em **`mission_history`** (`KAFKA_WORKFLOW`) e dispara **SSE** a cada passo.
4. **Falha:** `markFailure` grava **FALHA_PROCESSAMENTO** + histórico (`KAFKA_WORKFLOW_ERRO`) e notifica.
5. **Frontend:** **SSE** em `/api/missions/stream` + **polling** (12s) e refresh ao focar a aba.

## Estados

- Padrão: **RECEBIDA → EM_ANALISE → PRIORIZADA → EQUIPE_DESIGNADA → EM_ANDAMENTO → CONCLUIDA**.
- **CRÍTICA:** o primeiro passo após **RECEBIDA** é **PRIORIZADA** (pula **EM_ANALISE** na sequência definida pela strategy).
- Erro operacional do pipeline: **FALHA_PROCESSAMENTO**.

## Consultas principais

- **Painel:** `GET /api/missions/dashboard/summary` (métricas + recentes).
- **Detalhe + timeline:** `GET /api/missions/{id}` → `missao` + `historico` ordenado por tempo.
- **Só timeline (demo/API leve):** `GET /api/missions/{id}/history`.
- **Filtro demo:** `GET /api/missions/status/{status}` (enum `MissionStatus`).

## Fora de escopo N2

- Autenticação; WebSocket (usa-se **SSE**); fidelidade 100% ao Figma (vide `05-ui-e-dashboard.md`).
