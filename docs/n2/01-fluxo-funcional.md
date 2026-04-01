# N2 — Fluxo funcional (evoluído)

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
  F->>B: GET dashboard / detalhe (ou só SSE)
```

1. **Criação:** `POST /api/missions` persiste **missão** (RECEBIDA) e primeira linha em **`mission_history`** (origem `API_REGISTRO`).
2. **Kafka:** após commit, publica **`MISSION_CREATED`** em **`missions.created`** e notifica clientes **SSE** (`MissionRealtimeNotifier`).
3. **Consumer:** `MissionWorkflowService` aplica a **Strategy** (fluxo completo ou crítico), grava cada transição em **`mission_history`** (origem `KAFKA_WORKFLOW`) e dispara **SSE** a cada passo.
4. **Falha:** `markFailure` grava **FALHA_PROCESSAMENTO** + histórico (`KAFKA_WORKFLOW_ERRO`) e notifica.
5. **Frontend:** **SSE** em `/api/missions/stream` (evento `mission-update`) + **polling de backup** (12s) e refresh ao focar a aba.

## Estados

- Padrão: **RECEBIDA → EM_ANALISE → PRIORIZADA → EQUIPE_DESIGNADA → EM_ANDAMENTO → CONCLUIDA**.
- **CRÍTICA:** sem **EM_ANALISE** no primeiro passo do pipeline.
- Erro: **FALHA_PROCESSAMENTO**.

## Consultas principais

- **Painel:** `GET /api/missions/dashboard/summary` (métricas + recentes).
- **Detalhe + timeline:** `GET /api/missions/{id}` → `MissionDetailResponse` (`missao` + `historico` ordenado por tempo).

## Fora de escopo

- Autenticação; WebSocket (usamos **SSE** unidirecional); fidelidade 100% ao Figma (vide `05-ui-e-dashboard.md`).
