# N2 — Arquitetura Hexagonal (backend) — migração incremental

## Objetivo

Migrar o backend da Central-LJ para uma **arquitetura hexagonal pragmática**, preservando:

- endpoints REST e payloads já usados pelo frontend;
- tópicos Kafka (`missions.created` e `missions.events`);
- autenticação JWT e papéis;
- workflow assíncrono e timeline/histórico;
- SSE (`/api/missions/stream`).

## Visão do antes (camadas)

Estrutura original (simplificada):

- `controller` → `service` → `repository`
- `domain` (entidades JPA)
- `messaging` (Kafka producers/consumers + ingestão)
- `security` + `config`

## Visão do depois (hexagonal pragmática)

**Hexagonal pragmática** aqui significa:

- **Portas de entrada (in)**: interfaces explícitas que representam casos de uso.
- **Portas de saída (out)**: interfaces explícitas para dependências externas relevantes (mensageria, realtime etc.).
- **Adapters**: classes Spring existentes passam a implementar/consumir essas portas.
- **Sem “purificar o domínio JPA” nesta fase**: as entidades permanecem JPA para reduzir risco.

### Pacotes introduzidos

- `br.edu.central.centrallj.application.port.in.*`
  - portas de entrada (use cases) consumidas pelos controllers / adapters.
- `br.edu.central.centrallj.application.port.out.*`
  - portas de saída consumidas pela camada de aplicação.

### Como ficou no código (estado atual da migração)

Nesta etapa, fizemos uma evolução “de baixo risco”:

- Services existentes **implementam portas de entrada**:
  - `AuthService` → `LoginUseCase`, `GetCurrentUserUseCase`
  - `MissionCommandService` → `CreateMissionUseCase`
  - `MissionQueryService` → `MissionQueryUseCase`
  - `MissionAssignmentService` → `AssignMissionUseCase`
  - `MissionWorkflowService` → `ProcessMissionAfterCreationUseCase`
  - `HeroiService` → `HeroUseCase`
  - `EquipeHeroicaService` → `TeamUseCase`
- Controllers REST passam a depender das **interfaces** (ports/in), e não das classes concretas.
- Kafka:
  - `AfterCommitMissionDispatch` implementa a porta de saída `MissionPostCommitDispatchPort`.
  - `MissionRealtimeNotifier` implementa a porta de saída `MissionRealtimeNotifierPort`.
  - `MissionCreatedEventIngestionService` passa a depender de `ProcessMissionAfterCreationUseCase` (porta de entrada).

## Limites honestos desta fase

- Ainda não existe um “core domain puro” separado das entidades JPA.
- Ainda não existe uma pasta `adapters/*` completa; o projeto está numa transição incremental:
  - a separação por portas já existe,
  - os adapters estão “embutidos” nos pacotes atuais (ex.: `messaging`, `service`) para reduzir churn.

## Próximas ondas recomendadas

1. Consolidar adapters `in` e `out` em pacotes dedicados (sem mudar contratos).
2. Criar portas de saída para persistência (`MissionRepositoryPort`, etc.) e adaptar services/use cases para depender delas.
3. Refatorar Kafka para que o publisher seja sempre uma porta de saída (ex.: `MissionCreatedEventPublisherPort`) e o consumer seja um adapter `in` minimalista.
4. Se necessário (pós-banca), separar “domínio puro” de “entidades JPA”.

