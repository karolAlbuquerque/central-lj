# Central de Missões da Liga da Justiça (Central-LJ)

Monorepo da **Central de Missões da Liga da Justiça**: plataforma web para **registrar e acompanhar missões** (ciclo de vida, prioridade, Kafka, PostgreSQL), com **React**, **Spring Boot**, **Apache Kafka** e **PostgreSQL**.

---

## Status atual da N2 (evolução — painel, timeline, SSE, testes)

- **Persistência:** `missions` + **`mission_history`** (Flyway `V1` + `V2`); cada transição de status e falhas gera linha de histórico com origem (API / Kafka).
- **API:** `POST /api/missions`, `GET /api/missions`, **`GET /api/missions/dashboard/summary`**, **`GET /api/missions/stream` (SSE)**, `GET /api/missions/recent`, `GET /api/missions/{id}` (**detalhe + histórico**), `GET /api/missions/status/{status}`; health/hello; legados N1.
- **Kafka:** `missions.created` inalterado; consumer registra histórico e notifica **SSE** após cada passo.
- **Frontend:** **React Router** — **/** painel com métricas e tabela, **`/operacoes/nova`**, **`/missoes/:id`** com timeline; **SSE + polling 12s**; visual alinhado ao style guide.
- **Testes:** `MissionApiIntegrationTest`, `MissionWorkflowIntegrationTest` (ver [docs/n2/04-testes.md](docs/n2/04-testes.md)).
- **Docs:** [fluxo](docs/n2/01-fluxo-funcional.md), [decisões](docs/n2/02-decisoes-tecnicas.md), [pendências](docs/n2/03-pendencias.md), [testes](docs/n2/04-testes.md), [UI](docs/n2/05-ui-e-dashboard.md).

**Ordem para subir tudo:** Postgres + Kafka (Compose) → backend → frontend.

---

## Stack

| Camada | Tecnologia |
|--------|------------|
| Frontend | React 19 + Vite + TypeScript |
| Backend | Spring Boot 3.4 + Java 17 |
| Mensageria | Apache Kafka |
| Banco | PostgreSQL 16 (Compose) + Flyway |
| Infra local | Docker Compose + scripts PowerShell |

---

## Estrutura do repositório

```
central-lj/
├── README.md
├── docs/
│   ├── README.md
│   ├── n1/ …
│   ├── n2/              # Fluxo N2, decisões, pendências
│   ├── arquitetura/
│   ├── figma/
│   └── entrega/
├── infra/
├── backend/
├── frontend/
└── assets/
```

---

## Como rodar (N2)

### 1) Infra: PostgreSQL + Kafka + Kafka UI

Na raiz `central-lj/`:

```powershell
.\infra\scripts\up.ps1
```

- **PostgreSQL (host):** `localhost:5433` → container na 5432; banco `central_lj`, usuário/senha `central_lj` ([infra/kafka/README.md](infra/kafka/README.md)).
- **Kafka:** `localhost:9092`
- **Kafka UI:** http://localhost:8088

- A porta **5433** no host evita conflito com PostgreSQL local na **5432**.
- **Erro 28P01 (senha)?** Rode `.\infra\scripts\reset.ps1` e suba de novo; ou use **`mvn spring-boot:run -Dspring-boot.run.profiles=local`** (H2, sem Docker Postgres).
- Variáveis opcionais: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `CENTRAL_LJ_KAFKA_TOPIC_CREATED`.

### 2) Backend

```powershell
cd backend
mvn spring-boot:run
```

→ http://localhost:8080 — [backend/README.md](backend/README.md).

Na **primeira execução**, o Flyway aplica **`V1__create_missions.sql`** e **`V2__mission_history.sql`**.

### 3) Frontend

```powershell
cd frontend
npm install
npm run dev
```

→ http://localhost:5173 — [frontend/README.md](frontend/README.md).

---

## Endpoints principais

| Método | Caminho | Descrição |
|--------|---------|------------|
| GET | `/api/health` | Saúde do serviço |
| GET | `/api/hello` | Mensagem contextual |
| **POST** | **`/api/missions`** | **Cria missão (RECEBIDA) + histórico + evento Kafka** |
| **GET** | **`/api/missions`** | **Lista missões** |
| **GET** | **`/api/missions/dashboard/summary`** | **Métricas + recentes (painel)** |
| **GET** | **`/api/missions/stream`** | **SSE — eventos `mission-update`** |
| **GET** | **`/api/missions/recent`** | **Até 12 missões mais recentes** |
| **GET** | **`/api/missions/{id}`** | **Detalhe + `historico` (timeline)** |
| **GET** | **`/api/missions/status/{status}`** | **Filtra por status** |
| POST | `/api/missions/test` | Legado N1: ACK sem fluxo completo |
| POST | `/api/events/publish-test` | Legado N1: publica em `missions.events` |

---

## Tópicos Kafka

| Tópico | Uso |
|--------|-----|
| **`missions.created`** | Evento **MISSION_CREATED** após persistir nova missão; consumer executa workflow de status. |
| `missions.events` | Publicação manual de teste (`/api/events/publish-test`); consumer N1 apenas registra log. |

---

## Documentação

| Área | Onde |
|------|------|
| N1 | `docs/n1/`, `docs/entrega/` |
| N2 | `docs/n2/` |
| Arquitetura | `docs/arquitetura/*.puml` |

---

## Coerência

Alterações em contratos HTTP ou em tópicos devem ser refletidas neste README, em `docs/n2/*` e, quando aplicável, nos diagramas.

---

## Próximo passo sugerido (N2 cont.)

Ver [pendências N2](docs/n2/03-pendencias.md): autenticação, WebSocket/SSE, histórico de eventos, testes com Testcontainers, UI alinhada ao Figma.
