# N2 — Kafka em Ports & Adapters (backend)

## Fluxo atual preservado (contratos e tópicos)

- Tópico principal: **`missions.created`**
  - Payload: `MissionCreatedKafkaEvent` (`type = MISSION_CREATED`, `missionId`, snapshot leve).
  - Objetivo: disparar o workflow assíncrono após o commit da missão.
- Tópico auxiliar: **`missions.events`**
  - Usado para teste manual (`/api/events/publish-test`), consumer apenas loga.

## Onde Kafka entra na arquitetura

Na abordagem hexagonal:

- **Consumer Kafka** é um **adapter de entrada**:
  - recebe mensagens do broker (infra),
  - valida / parseia,
  - delega para uma **porta de entrada** (caso de uso).
- **Producer Kafka** é um **adapter de saída**:
  - a aplicação solicita “publicar evento” via **porta de saída**,
  - a implementação concreta usa `KafkaTemplate`.

## Portas introduzidas nesta fase

- `MissionPostCommitDispatchPort` (porta de saída)
  - responsável por “publicar + notificar” após commit.
  - Implementada por `AfterCommitMissionDispatch`.

- `ProcessMissionAfterCreationUseCase` (porta de entrada)
  - caso de uso chamado após consumo do evento `missions.created`.
  - Implementada por `MissionWorkflowService`.

## Adapters envolvidos

- Adapter in:
  - `MissionCreatedConsumer` (Spring `@KafkaListener`) continua “fino”.
  - `MissionCreatedEventIngestionService` faz parse/validação leve e chama `ProcessMissionAfterCreationUseCase`.

- Adapter out:
  - `AfterCommitMissionDispatch` acopla as garantias transacionais e chama:
    - `MissionCreatedEventProducer` (KafkaTemplate)
    - `MissionRealtimeNotifier` (SSE)

## Por que isso é defensável como P&A (pragmático)

- O consumer não contém regra de negócio; ele delega para um caso de uso.
- A publicação não é invocada diretamente pelo controller; ela ocorre em um componente pós-commit.
- A aplicação passou a enxergar a publicação/notify via **porta** (`MissionPostCommitDispatchPort`), reduzindo dependência direta de classes concretas.

## Próximo refinamento (quando houver tempo)

Separar a porta “dupla” (publish + notify) em duas portas explícitas:

- `MissionCreatedEventPublisherPort` (Kafka)
- `MissionRealtimeNotifierPort` (SSE)

mantendo `AfterCommitMissionDispatch` como adapter de infraestrutura que coordena “after commit”.

