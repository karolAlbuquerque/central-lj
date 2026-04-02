# Backend — Central-LJ (Spring Boot)

## Objetivo

API da Central-LJ: **missões** e **histórico (`mission_history`)** em PostgreSQL, **Kafka** (`missions.created`), consumer com evolução de status e **notificação SSE**; mantém endpoints **N1** para testes de arquitetura.

## Pré-requisitos

- **JDK 17+** (`java -version`). No Windows: se **`mvnw`** reclamar de `JAVA_HOME`, defina-a para a pasta do JDK ou instale **Temurin 17** (`winget install -e --id EclipseAdoptium.Temurin.17.JDK`) e **abra um novo PowerShell**.
- **Maven:** opcional no PATH — na pasta do backend use **`.\mvnw.cmd`** (Wrapper incluído no repositório).
- **PostgreSQL do projeto** (Docker Compose em **`localhost:5433`**), DB `central_lj`, usuário/senha `central_lj`
- **Kafka** em `localhost:9092` para o fluxo completo

### `mvn` não é reconhecido (PowerShell)

Use o wrapper (a partir da pasta `backend/`):

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

Ou instale Maven globalmente, por exemplo: `winget install -e --id Apache.Maven.Maven` (confira o nome exato com `winget search maven`), feche e reabra o terminal e use `mvn`.

### Erro `28P01` / “autenticação… falhou para o usuário central_lj”

1. `docker ps` → `central-lj-postgres`
2. URL padrão **`jdbc:postgresql://localhost:5433/central_lj`**
3. Volume antigo: `.\infra\scripts\reset.ps1` e `up.ps1`
4. Sem Docker: **`.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local`** (H2)

## Configuração (`application.yml`)

| Propriedade / env | Padrão | Função |
|-------------------|--------|--------|
| `spring.datasource.*` | `jdbc:postgresql://localhost:5433/central_lj` | Postgres |
| `spring.flyway` | habilitado | `V1` missões, `V2` histórico |
| `central-lj.kafka.topic-created` | `missions.created` | Domínio |
| `central-lj.kafka.topic` | `missions.events` | N1 |
| `central-lj.kafka.workflow-step-delay-ms` | `200` | Passos do workflow |
| `central-lj.kafka.consumer-enabled` | `true` / `false` no `test` | Listeners Kafka |

## Como rodar

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

→ http://localhost:8080

## Endpoints (N2 + legado)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/health` | Status |
| GET | `/api/hello` | Hello |
| **GET** | **`/api/missions/dashboard/summary`** | **Métricas + recentes** |
| **GET** | **`/api/missions/stream`** | **SSE (`mission-update`)** |
| **POST** | **`/api/missions`** | Cria missão + histórico + **MISSION_CREATED** |
| **GET** | **`/api/missions`** | Lista |
| **GET** | **`/api/missions/recent`** | Recentes (top 12) |
| **GET** | **`/api/missions/{id}`** | **Detalhe + timeline (`historico`)** |
| **GET** | **`/api/missions/{id}/history`** | **Somente timeline** |
| **GET** | **`/api/missions/status/{status}`** | Filtro |
| POST | `/api/missions/test` | N1 |
| POST | `/api/events/publish-test` | N1 · `missions.events` |

## Pacotes (visão)

`config`, `controller`, `dto`, `domain`, `service` (**MissionHistoryRecorder**, **MissionRealtimeNotifier**), `service.workflow`, `repository`, `messaging.consumer`, `messaging.producer`, `messaging.event`, `messaging.ingestion`, `messaging.support`, `exception`.

## Testes

```powershell
.\mvnw.cmd test
```

Cobertura HTML (JaCoCo): `target/site/jacoco/index.html`

- Integração: `MissionApiIntegrationTest`, `MissionWorkflowIntegrationTest`, `SmokeTest`
- Unitário: `MissionCreatedEventIngestionServiceTest`, `MissionProcessingFlowStrategyResolverTest`, `AfterCommitMissionDispatchTest`
- Perfil `test`: H2, Flyway off, `workflow-step-delay-ms: 0`, consumer Kafka off; **`@MockitoBean`** no producer onde aplicável

## Documentação

- Raiz: `../README.md`
- N2: `../docs/n2/`
