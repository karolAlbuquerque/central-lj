# Central de Missões da Liga da Justiça (Central-LJ)

Monorepo da **Central de Missões da Liga da Justiça**: plataforma web para **registrar e acompanhar missões** (ciclo de vida, prioridade, **Kafka**, **PostgreSQL**, **histórico auditável**), com **React**, **Spring Boot**, **Apache Kafka** e **SSE** no painel.

---

## Status N2 — fechamento

- **Fluxo:** criação **REST** → persistência + histórico → **commit** → **`missions.created` (Kafka)** → **consumer** (ingestão + workflow **Strategy**) → atualizações **SSE** e timeline na UI.
- **Elenco (MVP):** entidades **herói** e **equipe heroica**; missão com **atribuição opcional** a herói ou equipe; histórico com origem **`API_ATRIBUICAO`**; disponibilidade do herói pode ir a **EM_MISSAO** quando designado.
- **Autenticação (MVP):** **JWT** stateless; papéis **ADMIN** e **HERO** (reserva **OPERATOR**); vínculo **Usuario → Heroi** para contas de herói; **`POST /api/auth/login`**, **`GET /api/auth/me`**, **`GET /api/me/missions`**; seed demo opcional (`central-lj.auth.demo-seed`); frontend com **`/login`**, shell **administração** vs **área do herói** ([docs em `09` e `10`](docs/n2/09-autenticacao-e-papeis.md)).
- **Mensageria:** `MissionCreatedEventIngestionService`, `AfterCommitMissionDispatch`, consumers finos; tópico de domínio **`missions.created`** separado de **`missions.events`** (teste/observabilidade).
- **API (trecho):** missões, elenco e atribuição como antes; rotas administrativas e de atribuição exigem papel **ADMIN** ou **OPERATOR**; detalhe de missão e histórico exigem JWT; herói só consulta missão **designada a ele**. Ver tabela abaixo e [09-autenticacao-e-papeis.md](docs/n2/09-autenticacao-e-papeis.md).
- **Frontend:** após login, **ADMIN/OPERATOR** usam o **shell de coordenação** (sidebar + topbar de comando: Painel, Missões, Heróis, Equipes, Nova missão); **HERO** usa o **shell operacional** (Minha área, Minhas missões, Perfil heroico quando houver vínculo), com identidade visual distinta mas coerente. **Login** em tela dividida (branding + formulário). UI premium: tokens escuros azul-petróleo, alertas em vermelho, acentos ciano/dourado, componentes **`SectionCard`**, **`StatCard`**, **`Timeline`**, **`PriorityBadge`**, **`PageHeader`**, **`AppShell`** — ver [**11-redesign-ui-shell.md**](docs/n2/11-redesign-ui-shell.md) e [**05-ui-e-dashboard.md**](docs/n2/05-ui-e-dashboard.md). Tipografia: **Inter**, **Space Grotesk**, **IBM Plex Mono**.
- **Testes:** integração API + workflow + atribuição/heróis/equipes + **AuthApiIntegrationTest**; unitários (ingestão, strategy resolver, dispatch); **JaCoCo** (`backend/target/site/jacoco/index.html` após `.\mvnw.cmd test` no Windows).
- **Docs N2:** [fluxo](docs/n2/01-fluxo-funcional.md), [decisões](docs/n2/02-decisoes-tecnicas.md), [pendências](docs/n2/03-pendencias.md), [testes](docs/n2/04-testes.md), [UI / dashboards](docs/n2/05-ui-e-dashboard.md), [**preparação banca**](docs/n2/06-preparacao-banca.md), [**módulo heróis/equipes**](docs/n2/07-modulo-herois-equipes.md), [**atribuição**](docs/n2/08-atribuicao-de-missoes.md), [**auth**](docs/n2/09-autenticacao-e-papeis.md), [**área do herói**](docs/n2/10-area-do-heroi.md), [**redesign UI / shell**](docs/n2/11-redesign-ui-shell.md).
- **Docs N2:** [fluxo](docs/n2/01-fluxo-funcional.md), [decisões](docs/n2/02-decisoes-tecnicas.md), [pendências](docs/n2/03-pendencias.md), [testes](docs/n2/04-testes.md), [UI / dashboards](docs/n2/05-ui-e-dashboard.md), [**preparação banca**](docs/n2/06-preparacao-banca.md), [**módulo heróis/equipes**](docs/n2/07-modulo-herois-equipes.md), [**atribuição**](docs/n2/08-atribuicao-de-missoes.md), [**auth**](docs/n2/09-autenticacao-e-papeis.md), [**área do herói**](docs/n2/10-area-do-heroi.md), [**redesign UI / shell**](docs/n2/11-redesign-ui-shell.md), [**arquitetura hexagonal (backend)**](docs/n2/12-arquitetura-hexagonal.md), [**Kafka em ports & adapters**](docs/n2/13-portas-e-adaptadores-kafka.md).
- **Entrega / banca:** [resumo final N2](docs/entrega/resumo-final-n2.md), [checklist](docs/entrega/checklist-banca-final.md), [roteiro demo](docs/entrega/roteiro-demo-final.md).

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
│   ├── n1/ …
│   ├── n2/              # Fluxo N2, decisões, banca
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

- **PostgreSQL (host):** `localhost:5433` → banco `central_lj`, usuário/senha `central_lj`.
- **Kafka:** `localhost:9092`
- **Kafka UI:** http://localhost:8088

Postgres inclui **healthcheck** (`pg_isready`) para ambientes que aguardam o banco ficar pronto.

Variáveis opcionais: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `CENTRAL_LJ_KAFKA_TOPIC_CREATED`, `CENTRAL_LJ_WORKFLOW_STEP_DELAY_MS`.

### 2) Backend

**Pré-requisito:** **JDK 17+** instalado. No Windows, se aparecer erro de `JAVA_HOME`, configure a variável apontando para a pasta do JDK (ex.: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`) ou instale o Temurin 17:

```powershell
winget install -e --id EclipseAdoptium.Temurin.17.JDK
```

**Maven:** este repositório inclui o **Maven Wrapper** — não é obrigatório ter `mvn` no PATH.

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Se você já tem Maven instalado globalmente, pode usar `mvn spring-boot:run` em vez de `.\mvnw.cmd`.

→ http://localhost:8080 — [backend/README.md](backend/README.md).

### 3) Frontend

```powershell
cd frontend
npm install
npm run dev
```

→ http://localhost:5173 — [frontend/README.md](frontend/README.md). A UI redireciona para **`/login`** se não houver token; após entrar, **coordenador** vai ao painel e **herói** à **área do herói**.

**Contas de demonstração** (primeira subida com banco vazio e `central-lj.auth.demo-seed=true`): ver [docs/n2/09-autenticacao-e-papeis.md](docs/n2/09-autenticacao-e-papeis.md) — por exemplo `coordenacao@central-lj.demo` / `Admin@demo2026` e `heroi.demo@central-lj.demo` / `Hero@demo2026`.

**Migração:** ao atualizar de uma versão anterior do repositório, aplique o Flyway até **`V4__auth_usuarios`** (tabela `usuarios`).

---

## Testes (backend)

```powershell
cd backend
.\mvnw.cmd test
```

Relatório de cobertura: abrir `backend/target/site/jacoco/index.html` após os testes.

---

## Endpoints principais

| Método | Caminho | Descrição |
|--------|---------|------------|
| GET | `/api/health` | Saúde do serviço |
| GET | `/api/hello` | Mensagem contextual |
| **POST** | **`/api/auth/login`** | **Autenticação — retorna JWT + dados do usuário** |
| **GET** | **`/api/auth/me`** | **Usuário atual (Bearer token)** |
| POST | `/api/auth/logout` | Logout stateless (cliente descarta token) |
| **GET** | **`/api/me/missions`** | **Missões do herói vinculado ao token (HERO)** |
| **POST** | **`/api/missions`** | **Cria missão (RECEBIDA) + histórico + evento Kafka (após commit)** |
| **GET** | **`/api/missions`** | **Lista missões** |
| **PATCH** | **`/api/missions/{id}/assign-hero`** | **Designa herói responsável + histórico `API_ATRIBUICAO`** |
| **PATCH** | **`/api/missions/{id}/assign-team`** | **Designa equipe responsável + histórico `API_ATRIBUICAO`** |
| **GET** | **`/api/missions/dashboard/summary`** | **Métricas + recentes** |
| **GET** | **`/api/missions/stream`** | **SSE — eventos `mission-update`** |
| **GET** | **`/api/missions/recent`** | **Até 12 missões mais recentes** |
| **GET** | **`/api/missions/status/{status}`** | **Filtra por status** |
| **GET** | **`/api/missions/{id}/history`** | **Somente timeline** |
| **GET** | **`/api/missions/{id}`** | **Detalhe + `historico` + `atribuicao`** |
| POST | `/api/heroes` | Cadastro de herói |
| GET | `/api/heroes` | Lista heróis |
| GET | `/api/heroes/{id}` | Detalhe do herói |
| GET | `/api/heroes/{id}/missions` | Missões em que o herói é responsável |
| PATCH | `/api/heroes/{id}/availability` | Atualiza disponibilidade |
| POST | `/api/teams` | Cadastro de equipe |
| GET | `/api/teams` | Lista equipes |
| GET | `/api/teams/{id}` | Detalhe + membros |
| POST | `/api/missions/test` | Legado N1 |
| POST | `/api/events/publish-test` | Legado N1 · `missions.events` |

---

## Tópicos Kafka

| Tópico | Uso |
|--------|-----|
| **`missions.created`** | **`MISSION_CREATED`** após commit da nova missão; consumer executa workflow. |
| `missions.events` | Publicação manual de teste; consumer apenas observa (log/debug). |

---

## Coerência

Alterações em contratos HTTP ou em tópicos devem ser refletidas neste README, em `docs/n2/*` e, quando aplicável, nos diagramas.

---

## Próximo passo sugerido (pós-N2)

Ver [pendências](docs/n2/03-pendencias.md): autenticação, Testcontainers, Figma pixel-perfect, OpenAPI, reforço de idempotência em produção.
