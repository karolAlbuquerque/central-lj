# Central-LJ — Documentação Completa do Sistema

**Projeto Integrador PUC Goiás 2026/1 — ADS 4º Período**

---

## O que o documento exige

O Documento Norteador pede uma **Solução Web Distribuída com Mensageria/Streams** que integre quatro disciplinas:

| Disciplina | Exigência |
|---|---|
| Modelagem de Interfaces (UI) | Design System, Figma, prototipagem de alta fidelidade |
| Design de Software | Arquitetura Hexagonal ou Microsserviços, DDD, Design Patterns, C4 Model |
| Desenvolvimento Web | Full-stack Spring Boot + React, API REST |
| **Mensageria e Streams** | **Fila real (Kafka/RabbitMQ), consumer, atualização em tempo real via WebSocket/SSE** |

O critério mais pesado na N2 é **Implementação Técnica: 0,8pt** — sendo **0,4pt exclusivamente para a mensageria desacoplada**. O documento deixa claro: "não será aceito o CRUD simples".

---

## Como cada disciplina foi aplicada no Central-LJ

### 1. Modelagem de Interfaces (UI)

O sistema tem um Design System completo implementado com **CSS Modules** e **tokens visuais** aplicados consistentemente:

- **3 layouts distintos por papel**: `AppLayout` (admin/operator), `HeroLayout` (herói), `VillainLayout` (vilão) — cada um com paleta, tipografia e navegação próprias
- **17 páginas** implementadas, todas fiéis ao Figma documentado em `docs/figma/style-guide.md`
- **Componentes reutilizáveis**: `StatusBadge`, `PriorityBadge`, `StatCard`, `Timeline`, `SectionCard`, `PageHeader` — construídos como peças do Design System
- **10 tipos de puzzle interativos**: `WireConnectPuzzle`, `ArcadeShooterPuzzle`, `SequenceMemoryPuzzle`, `CardMatchPuzzle`, `SlidingTilePuzzle`, `TerminalHackPuzzle`, etc. — UX gamificada com feedback visual em tempo real
- **Visualizador 3D GLTF** via `model-viewer`/Three.js nas páginas de herói
- **Feedback de status assíncrono**: a tela atualiza automaticamente conforme o Kafka processa a missão (sem o usuário recarregar)

### 2. Design de Software

**Arquitetura Hexagonal** implementada estritamente em `br.edu.central.centrallj`:

```
domain/          ← Entidades puras, sem dependência de framework
application/
  port/in/       ← 19 Use Cases (interfaces)
  port/out/      ← 13 Ports de saída (interfaces)
  service/       ← Implementações dos Use Cases
adapter/
  in/web/        ← Controllers REST (entrada HTTP)
  in/messaging/  ← Consumers Kafka (entrada mensageria)
  out/persistence/ ← JPA (saída banco)
  out/messaging/ ← Producers Kafka (saída mensageria)
  out/realtime/  ← SSE (saída tempo real)
```

**Por que Hexagonal aqui?** A disciplina do broker fica em `adapter/out/messaging` — trocar Kafka por RabbitMQ não toca nenhuma linha de lógica de negócio. O `MissionEventPublishPort` é uma interface pura; o `KafkaMissionEventPublishAdapter` é só uma implementação dela.

**Design Patterns aplicados:**

| Pattern | Onde |
|---|---|
| **Strategy** | `MissionProcessingFlowStrategy` — fluxo padrão vs. fluxo crítico |
| **Factory** | `MissionCreatedEventFactory` — constrói o evento de domínio |
| **Adapter** | Toda a camada `adapter/` — entrada e saída |
| **Template Method** | `MissionWorkflowService.processAfterCreation()` — loop de estados |
| **Command** | `CreateMissionCommand`, `ExecuteTaskCommand`, etc. |

**Documentação arquitetural**: C4 Contexto + C4 Container em PlantUML gerados (`.puml` + `.png`), Diagrama de Classes, Diagrama ER do banco e o Diagrama de Pipeline Kafka — tudo em `docs/arquitetura/`.

**SOLID aplicado:**
- **S**: cada service tem uma responsabilidade (`MissionCommandService` cria, `MissionWorkflowService` processa, `MissionQueryService` consulta)
- **O**: `MissionProcessingFlowStrategy` é extensível sem modificar o workflow
- **D**: services dependem de interfaces (ports), não de implementações concretas

### 3. Desenvolvimento Web

**Backend: Spring Boot 3.4 + Java 17**
- 12 controllers REST com 40+ endpoints
- Spring Security 6 + JWT HS256 stateless
- JPA + Flyway (V1 a V17) gerenciando 11 tabelas no PostgreSQL 16
- `@Transactional` em todas as operações de escrita
- `GlobalExceptionHandler` com respostas padronizadas

**Frontend: React 19 + TypeScript + Vite 6**
- Context API para autenticação (`AuthContext`)
- Fetch manual para dados REST
- Hook `useMissionUpdates` para tempo real (detalhado abaixo)
- CSS Modules — zero dependência de biblioteca CSS externa

**Fluxo completo conforme exigido:**
`Cadastro (POST /api/missions) → Persistência (PostgreSQL) → Fila (Kafka) → Workflow → Atualização na tela (SSE)`

---

## 4. Mensageria e Streams — Foco principal

Este é o coração técnico do projeto. O pipeline está completamente implementado.

### Visão geral do pipeline

```
[React] POST /api/missions
    ↓
[MissionCommandService] @Transactional
    → INSERT missions (status=RECEBIDA)
    → INSERT mission_history (API_REGISTRO)
    → COMMIT JPA
    ↓
[AfterCommitMissionDispatch] — só dispara após COMMIT bem-sucedido
    → KafkaTemplate.send("missions.created", missionId, JSON)
    → SseEmitter.send("mission-update")  ← frontend já recarrega
    ↓
[KAFKA BROKER — tópico: missions.created]
    ↓
[MissionCreatedConsumer] @KafkaListener
    → MissionCreatedEventIngestionService.ingestJson(payload)
    → deserializa JSON → valida missionId e type
    → ProcessMissionCreatedUseCase.processAfterCreation(missionId)
    ↓
[MissionWorkflowService]
    → MissionProcessingFlowStrategyResolver.resolve(mission)
    ↓
    ┌─────────────────────────────┬──────────────────────────────────┐
    │ DefaultFlow (baixa/média/alta)│ CriticalFlow (CRÍTICA)          │
    │ EM_ANALISE                   │ PRIORIZADA                       │
    │ PRIORIZADA                   │ EQUIPE_DESIGNADA                 │
    │ EQUIPE_DESIGNADA             │ EM_ANDAMENTO                     │
    │ EM_ANDAMENTO                 │ CONCLUIDA                        │
    │ CONCLUIDA                    │                                  │
    └─────────────────────────────┴──────────────────────────────────┘
    Para cada estado:
    → UPDATE missions.status
    → INSERT mission_history (KAFKA_WORKFLOW)
    → SseEmitter.send("mission-update")  ← frontend atualiza a cada step
    → sleep(200ms)  ← delay configurável para visualização na demo
```

### A garantia transacional — detalhe crítico

O ponto mais importante do pipeline é o `AfterCommitMissionDispatch`:

```java
// AfterCommitMissionDispatch.java
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronization() {
        @Override
        public void afterCommit() {
            missionEventPublishPort.publishMissionCreated(event);
            missionNotificationPort.notifyMissionUpdate(missionId);
        }
    });
```

**Por que isso importa:** se o Kafka recebesse o evento *antes* do `COMMIT`, o consumer tentaria processar uma missão que ainda não existe no banco — race condition clássica. Com `afterCommit()`, o evento Kafka só é publicado **após** a transação JPA ter feito commit com sucesso. Se a transação der rollback, nenhum evento é publicado. Isso é **exactly-once semântica no lado do produtor**.

### Os dois tópicos Kafka

| Tópico | Propósito | Consumer |
|---|---|---|
| `missions.created` | Pipeline principal — dispara o workflow de estados | `MissionCreatedConsumer` → `MissionWorkflowService` |
| `missions.events` | Observabilidade e debug genérico | `MissionEventsConsumer` (apenas loga) |

Configurados via propriedades externalizáveis:

```yaml
central-lj:
  kafka:
    topic: ${CENTRAL_LJ_KAFKA_TOPIC:missions.events}
    topic-created: ${CENTRAL_LJ_KAFKA_TOPIC_CREATED:missions.created}
    consumer-enabled: true          # false no perfil de teste (H2)
    workflow-step-delay-ms: 200     # delay entre steps para visualização
```

### O desacoplamento via Port (ponto de 0,4pt na avaliação)

```java
// Port — pura interface, sem Kafka
public interface MissionEventPublishPort {
    void publishMissionCreated(MissionCreatedEvent event);
}

// Adapter — implementação Kafka, substituível
@Component
public class KafkaMissionEventPublishAdapter implements MissionEventPublishPort {
    private final MissionCreatedEventProducer producer;

    @Override
    public void publishMissionCreated(MissionCreatedEvent event) {
        producer.publish(new MissionCreatedKafkaEvent(...));
    }
}
```

O `MissionWorkflowService` conhece apenas `MissionNotificationPort` e `MissionPersistencePort` — **nenhuma referência a Kafka existe dentro do domínio ou dos services de aplicação**. Isso é o que o documento chama de "mensageria desacoplada corretamente".

### O Strategy Pattern para diferentes fluxos

```java
// MissionProcessingFlowStrategyResolver.java
public MissionProcessingFlowStrategy resolve(Mission mission) {
    return mission.getPrioridade() == PrioridadeMissao.CRITICA
        ? criticalFlow   // pula EM_ANALISE — despacho acelerado
        : defaultFlow;   // fluxo completo com análise
}
```

Missões CRÍTICAS pulsam 4 estados, missões normais pulsam 5 — visível na interface em tempo real. O Strategy Pattern aqui serve diretamente ao negócio, não apenas como demonstração de pattern.

### O lado do frontend — hook `useMissionUpdates`

```typescript
// useMissionUpdates.ts
export function useMissionUpdates(onRefresh, pollMs = 12000) {
    useEffect(() => {
        // 1. SSE — notificação imediata do backend
        void consumeMissionUpdates(controller.signal, run);

        // 2. Polling fallback — segurança se SSE cair
        const interval = window.setInterval(run, pollMs);

        // 3. visibilitychange — recarrega ao voltar à aba
        document.addEventListener("visibilitychange", onVis);
    }, [pollMs]);
}
```

**Três mecanismos de atualização em camadas:**
1. **SSE primário** — atualiza em tempo real conforme cada transição de estado chega do Kafka
2. **Polling de 12s** — garante consistência se a conexão SSE cair
3. **visibilitychange** — recarrega ao focar a aba, eliminando dados stale

O SSE usa `text/event-stream` nativo do browser (sem biblioteca), parseado manualmente para detectar `event:mission-update`.

### Infraestrutura Kafka no Docker

```yaml
# infra/docker-compose.yml
kafka:
  image: apache/kafka:3.7.2   # KRaft mode — sem ZooKeeper
  ports: ["9092:9092"]

kafka-ui:
  image: provectuslabs/kafka-ui:latest
  ports: ["8088:8080"]        # Interface visual para demo/banca
  environment:
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
```

**Kafka KRaft** (sem ZooKeeper): a versão 3.7.2 já roda em modo standalone sem dependência do ZooKeeper — container único, mais simples para demo e banca. O **Kafka UI** na porta 8088 permite mostrar visualmente os tópicos, mensagens e consumer group durante a apresentação.

### Testes do pipeline de mensageria

```java
// MissionCreatedEventIngestionServiceTest.java

@Test
void ingestJsonValidoDelegaWorkflow() throws Exception {
    // Dado um JSON válido de MissionCreatedKafkaEvent
    ingestionService.ingestJson(json);
    // Verifica que o workflow foi acionado com o missionId correto
    verify(processMissionCreatedUseCase).processAfterCreation(eq(id));
}

@Test
void ingestIgnoraTipoDiferente() {
    // Evento com type="OUTRO" não dispara workflow
    verify(processMissionCreatedUseCase, never()).processAfterCreation(any());
}

@Test
void ingestIgnoraMissionIdNulo() {
    // Payload inválido é descartado silenciosamente
    verify(processMissionCreatedUseCase, never()).processAfterCreation(any());
}
```

Cobertura com JaCoCo configurada no `pom.xml`. Testes de integração (`MissionWorkflowIntegrationTest`, `MissionApiIntegrationTest`) cobrem o fluxo ponta-a-ponta com H2 em memória e Kafka desabilitado via `consumer-enabled: false`.

---

## Resumo do mapeamento disciplina × critério de avaliação

| Critério N2 | Pts | Como está coberto |
|---|---|---|
| Software funciona? | 0,4 | Fluxo completo: cadastro → fila → workflow → tela |
| **Mensageria desacoplada?** | **0,4** | **Port `MissionEventPublishPort` separa domínio de Kafka; `AfterCommitMissionDispatch` garante consistência transacional** |
| Design Patterns e Clean Architecture | 0,5 | Hexagonal + Strategy + Factory + Command; 19 Use Cases via interfaces |
| Testes rodando | 0,4 | Unit tests (Mockito), integration tests (H2), JaCoCo report |
| Interface fiel ao protótipo | 0,3 | CSS Modules, Design System, 17 páginas com 3 layouts de papel |

---

## Pergunta provável na arguição

**"Por que o evento Kafka só é publicado após o COMMIT?"**

A resposta está no `AfterCommitMissionDispatch` com `TransactionSynchronizationManager.registerSynchronization()`. Se o evento fosse publicado dentro da transação (antes do commit), o consumer Kafka poderia tentar processar uma missão que ainda não existe no banco — race condition clássica em sistemas distribuídos. Com `afterCommit()`, a publicação só ocorre depois que a transação JPA persiste com sucesso. Rollback = sem evento publicado.
