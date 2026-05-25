# Central-LJ

**Central de Missões da Liga da Justiça** — plataforma web para registrar, priorizar e acompanhar missões com **ciclo de vida assíncrono**, **histórico auditável** e **dois perfis de uso** (coordenação e herói em campo).

Projeto acadêmico de arquitetura distribuída: não é um CRUD simples — missões são **processos com estado**, evoluídos via **Kafka** após a API persistir e confirmar a transação.

---

## O que o sistema faz

| Papel | O que faz na prática |
|-------|----------------------|
| **Admin / Operador** | Dashboard, criar missões, atribuir herói ou equipe, gerenciar elenco |
| **Herói** | Ver **somente** missões designadas a ele, acompanhar timeline e status |
| **Kafka (invisível)** | Após criar missão, avança status automaticamente até `CONCLUIDA` |
| **Histórico** | Registra cada transição (API, atribuição ou workflow) |

Fluxo resumido: **REST → PostgreSQL → commit → `missions.created` → consumer → Strategy → SSE na UI**.

Documentação completa: [**docs/visao-do-sistema.md**](docs/visao-do-sistema.md)

---

## Stack

| Camada | Tecnologia |
|--------|------------|
| Frontend | React 19 · Vite · TypeScript |
| Backend | Spring Boot 3.4 · Java 17+ · **arquitetura hexagonal** |
| Banco | PostgreSQL 16 · Flyway |
| Mensageria | Apache Kafka |
| Tempo real | SSE (`/api/missions/stream`) |
| Auth | JWT stateless · papéis `ADMIN` · `HERO` · `OPERATOR` (reserva) |
| Infra local | Docker Compose |

---

## Arquitetura

```text
┌─────────────┐     REST + JWT      ┌──────────────────────────────┐
│  React SPA  │ ◄──────────────────►│  Spring Boot (hexagonal)     │
│  :5173      │     SSE stream      │  :8080                       │
└─────────────┘                     └──────────┬───────────────────┘
                                               │
                         ┌─────────────────────┼─────────────────────┐
                         ▼                     ▼                     ▼
                   PostgreSQL              Kafka                 Flyway
                   :5433                   :9092
```

**Backend (ports & adapters):**

```text
adapter/in/web          → REST, DTOs, JWT
adapter/in/messaging    → consumers Kafka
application/            → use cases, domain puro, regras
adapter/out/persistence → JPA, repositórios
adapter/out/messaging   → produtor Kafka
adapter/out/realtime    → SSE
```

Diagramas e papéis detalhados: [docs/visao-do-sistema.md](docs/visao-do-sistema.md) · [docs/arquitetura/](docs/arquitetura/)

---

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (PostgreSQL + Kafka)
- **JDK 17+** (`java -version`)
- **Node.js 18+** (`node -version`)

---

## Como rodar

Suba nesta ordem: **infra → backend → frontend**.

### 1. Infraestrutura

```bash
cd infra
docker compose up -d
docker compose ps   # postgres, kafka e kafka-ui devem estar Up
```

| Serviço | Endereço |
|---------|----------|
| PostgreSQL | `localhost:5433` · DB `central_lj` · user/pass `central_lj` |
| Kafka | `localhost:9092` |
| Kafka UI | http://localhost:8088 |

<details>
<summary>Windows (PowerShell)</summary>

```powershell
.\infra\scripts\up.ps1
```

</details>

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

Aguarde **`Started CentralLjApplication`** → http://localhost:8080

<details>
<summary>Windows</summary>

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

</details>

> **Porta 8080 ocupada?** Já há um backend rodando — use http://localhost:8080 ou encerre o processo: `lsof -ti :8080 | xargs kill`

### 3. Frontend

```bash
cd frontend
npm install    # primeira vez
npm run dev
```

→ http://localhost:5173 (redireciona para `/login` sem token)

---

## Login demo

Contas criadas automaticamente na primeira subida (`central-lj.auth.demo-seed=true`):

| Papel | E-mail | Senha |
|-------|--------|-------|
| Admin / coordenação | `coordenacao@central-lj.demo` | `Admin@demo2026` |
| Herói | `heroi.demo@central-lj.demo` | `Hero@demo2026` |

Após login: **admin** → painel de coordenação · **herói** → `/heroi/area`

---

## Testes

```bash
cd backend
./mvnw test
```

Cobertura JaCoCo: `backend/target/site/jacoco/index.html`

---

## Estrutura do repositório

```text
central-lj/
├── backend/          # API Spring Boot (hexagonal)
├── frontend/         # SPA React
├── infra/            # Docker Compose (Postgres, Kafka, Kafka UI)
├── docs/
│   ├── visao-do-sistema.md   # visão geral (recomendado)
│   ├── n2/                   # fluxo, auth, UI, banca
│   ├── arquitetura/          # diagramas C4
│   └── entrega/              # checklists e roteiro demo
└── assets/
```

---

## API principal

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/auth/login` | Login → JWT |
| GET | `/api/auth/me` | Usuário autenticado |
| GET | `/api/me/missions` | Missões do herói logado |
| POST | `/api/missions` | Cria missão + Kafka após commit |
| GET | `/api/missions/dashboard/summary` | Métricas do painel |
| GET | `/api/missions/stream` | SSE `mission-update` |
| GET | `/api/missions/{id}` | Detalhe + timeline |
| PATCH | `/api/missions/{id}/assign-hero` | Designa herói |
| PATCH | `/api/missions/{id}/assign-team` | Designa equipe |
| POST/GET | `/api/heroes` · `/api/teams` | Elenco heroico |

Rotas administrativas exigem `ADMIN` ou `OPERATOR`. Herói só acessa missões **atribuídas a ele**.

Detalhes de auth: [docs/n2/09-autenticacao-e-papeis.md](docs/n2/09-autenticacao-e-papeis.md)

---

## Kafka

| Tópico | Uso |
|--------|-----|
| **`missions.created`** | Workflow automático após criar missão |
| `missions.events` | Teste/observabilidade (legado N1) |

---

## Parar tudo

```bash
# Infra
cd infra && docker compose down

# Backend e frontend: Ctrl+C nos terminais
```

---

## Documentação

| Documento | Conteúdo |
|-----------|----------|
| [visao-do-sistema.md](docs/visao-do-sistema.md) | Intuito, fluxos, papéis, arquitetura |
| [01-fluxo-funcional.md](docs/n2/01-fluxo-funcional.md) | Sequência técnica detalhada |
| [09-autenticacao-e-papeis.md](docs/n2/09-autenticacao-e-papeis.md) | JWT, roles, seed demo |
| [roteiro-demo-final.md](docs/entrega/roteiro-demo-final.md) | Roteiro para apresentação |
| [backend/README.md](backend/README.md) | Configuração e pacotes do backend |
| [frontend/README.md](frontend/README.md) | Proxy Vite e build |

---

## Licença e contexto

Projeto acadêmico — **Central-LJ** (N1 + N2). Tema Liga da Justiça como metáfora para centrais de comando orientadas a eventos.
