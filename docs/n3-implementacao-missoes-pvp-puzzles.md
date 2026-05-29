# N3 — Plano de implementação: missões colaborativas com vilões e puzzles

## Objetivo

Evoluir a Central-LJ de um fluxo majoritariamente automatizado para um modelo de missões orientado por jogadores:

- herói pode criar missão e se tornar chefe da operação;
- chefe distribui papéis e tarefas por missão;
- vilão enxerga missões ativas e tenta infiltração por duelo de puzzles;
- resultado do duelo impacta a missão com regras auditáveis.

Este plano preserva a base técnica existente (Spring + PostgreSQL + Kafka + SSE + React) e impõe aderência total ao padrão visual/arquitetural atual do frontend.

---

## Escopo funcional consolidado

### 1) Papéis globais de conta

- `ADMIN`: governança/moderação e visão macro.
- `HERO`: jogador herói.
- `VILLAIN`: jogador vilão.
- `OPERATOR` (legado): mantido apenas para rotas existentes; **não recebe acesso às novas rotas de missão/duelo**. Sem novas funcionalidades para este papel.

### 2) Papéis por missão

- `CHEFE`: usuário criador da missão; único com permissão de gerenciar membros e tarefas.
- `HERO_MEMBER`: herói participante convidado pelo chefe.
- `VILLAIN_INTRUDER`: vilão que venceu duelo de infiltração; acesso somente-leitura à missão + pode escolher vetor de sabotagem.

### 3) Fluxo principal da missão

1. Herói cria missão e vira `CHEFE`.
2. Chefe convida heróis → cada convite gera `MissionMember` com role `HERO_MEMBER`.
3. Chefe define tarefas e dependências entre elas.
4. Heróis executam tarefas; tarefas críticas exigem puzzle validado no backend.
5. Vilões tentam infiltração; sistema dispara alerta ao defensor designado.
6. Duelo de puzzle define defesa ou sabotagem com TTL.
7. Missão fecha por objetivos concluídos, comprometimento grave ou encerramento manual do chefe.

---

## Fase 0 — Decisões de fundação (pré-implementação)

Estas decisões devem ser registradas e aprovadas antes de qualquer código das fases seguintes.

### Decisão 1 — Protocolo de comunicação em tempo real

**Decisão:** WebSocket (STOMP sobre SockJS) para o módulo de duelo; SSE mantido para eventos de missão não-críticos de latência.

**Justificativa:** duelo exige comunicação bidirecional em < 200 ms. SSE é unidirecional e exigiria polling ou workarounds. Eventos de missão (convite, tarefa atribuída, alerta de infiltração) tolerem latência de segundos e continuam via SSE/Kafka.

**Impacto:** adicionar `spring-boot-starter-websocket`; configurar `WebSocketMessageBrokerConfigurer`; cliente React usa `@stomp/stompjs`.

### Decisão 2 — Protocolo do seed de puzzle (anti-cheat)

O seed é gerado exclusivamente pelo backend no momento de criação do `DuelSession`. Fluxo:

1. Backend gera `seed` (UUID v4 + timestamp em ms).
2. Seed é enviado para ambos os participantes via WS no evento `duel:started`.
3. Cada cliente gera a sequência do puzzle localmente a partir do seed usando algoritmo determinístico documentado (ex: LCG com parâmetros fixos).
4. Cliente envia apenas os resultados (respostas/movimentos), nunca a sequência gerada.
5. Backend re-deriva a sequência a partir do seed e valida cada `SubmitPuzzleProgress` no servidor.
6. Qualquer resposta que não corresponda à sequência esperada é rejeitada com `INVALID_MOVE`; três rejeições consecutivas = disqualificação automática.

### Decisão 3 — Mecanismo de lock de duelo

Lock pessimista no banco via `SELECT ... FOR UPDATE` na linha de `DuelSession` para operações de encerramento/transição de estado. Justificativa: stack já usa PostgreSQL; evita dependência adicional de Redis para este caso.

Para múltiplos vilões tentando infiltrar a mesma missão simultaneamente: apenas um `DuelSession` com status `PENDING` ou `ACTIVE` pode existir por missão ao mesmo tempo. Tentativa de segunda infiltração retorna `409 CONFLICT` com mensagem `"missão já em duelo ativo"`.

### Decisão 4 — Catálogo inicial de PuzzleType

| Tipo | Descrição | Regra de validação no backend |
|---|---|---|
| `DRAG_SORT` | Ordenar itens na sequência correta | Array de IDs enviado deve corresponder à sequência derivada do seed |
| `NODE_CONNECT` | Conectar nós de origem a destino correto | Array de pares `{from, to}` deve corresponder ao mapa derivado do seed |
| `SEQUENCE_INPUT` | Digitar sequência de códigos na ordem | Array de strings deve ser igual ao array derivado do seed |

Fase avançada pode adicionar `ARCADE` (Phaser), mas não faz parte das Fases 1–3.

### Decisão 5 — Cooldowns e limites

| Evento | Cooldown | Escopo |
|---|---|---|
| Vilão perde duelo | 10 minutos | Global (não pode iniciar novo duelo) |
| Vilão vence duelo | 5 minutos | Por missão (não pode reinfiltrá-la imediatamente) |
| Herói reconvida membro que recusou | 30 minutos | Por membro/missão |
| Troca de papel (`HERO` ↔ `VILLAIN`) | 24 horas | Global; exige que usuário não tenha missão ativa como chefe |

Cooldowns persistidos em campo `available_at TIMESTAMP` na tabela do usuário ou em tabela `user_cooldown` separada. Não usar cache em memória (reinício de aplicação zera cooldowns).

### Decisão 6 — Fluxos de exceção do duelo

| Situação | Comportamento |
|---|---|
| Defensor offline no momento do alerta | Janela de 3 minutos para aceitar; expirado → vitória automática do vilão (best-of-1) ou round perdido (best-of-3) |
| Desconexão de qualquer lado durante duelo ativo | 60 s para reconexão via WS; expirado → lado desconectado perde o round/duelo |
| Dois vilões infiltram simultaneamente | Segundo recebe `409`; primeiro inicia normalmente |
| Backend não valida move por seed corrompido | `DuelSession` marcada `CANCELLED`; nenhum efeito aplicado; evento `duel:cancelled` emitido |
| Chefe offline sem sucessor | Missão entra em estado `SEM_CHEFE`; membros podem eleger novo chefe por votação simples (maioria) |

---

## Requisitos de arquitetura e domínio

## Backend (Spring Boot)

### Novas entidades — campos obrigatórios

#### `MissionMember`

```
id                  UUID PK
mission_id          UUID FK → Mission
user_id             UUID FK → User
role                MissionMemberRole (CHEFE, HERO_MEMBER, VILLAIN_INTRUDER)
joined_at           TIMESTAMP
invited_by_user_id  UUID FK → User (nullable; null se for o próprio chefe)
```

Constraint: combinação `(mission_id, user_id)` única.

#### `MissionTask`

```
id                  UUID PK
mission_id          UUID FK → Mission
assigned_to_user_id UUID FK → User (nullable enquanto não atribuída)
title               VARCHAR(200)
description         TEXT
status              TaskStatus (PENDING, IN_PROGRESS, AWAITING_PUZZLE, DONE, BLOCKED)
is_critical         BOOLEAN DEFAULT false  -- tarefas críticas exigem puzzle
depends_on_task_id  UUID FK → MissionTask (nullable; dependência simples, sem ciclo)
created_at          TIMESTAMP
updated_at          TIMESTAMP
```

#### `DuelSession`

```
id                  UUID PK
mission_id          UUID FK → Mission
attacker_user_id    UUID FK → User  -- vilão
defender_user_id    UUID FK → User  -- herói designado pelo chefe ou o próprio chefe
seed                VARCHAR(64)     -- gerado pelo backend; imutável após criação
puzzle_type         PuzzleType
status              DuelStatus
round_current       SMALLINT DEFAULT 1
round_max           SMALLINT DEFAULT 1  -- 1 para best-of-1; 3 para best-of-3
attacker_rounds_won SMALLINT DEFAULT 0
defender_rounds_won SMALLINT DEFAULT 0
started_at          TIMESTAMP
finished_at         TIMESTAMP (nullable)
timeout_at          TIMESTAMP       -- deadline do round atual
```

#### `PuzzleAttempt`

```
id                  UUID PK
duel_session_id     UUID FK → DuelSession
user_id             UUID FK → User
round_number        SMALLINT
move_sequence       JSONB           -- array de movimentos enviados pelo cliente
is_valid            BOOLEAN         -- resultado da validação do backend
time_ms             INTEGER         -- tempo de conclusão em ms
submitted_at        TIMESTAMP
```

#### `SabotageEvent`

```
id                  UUID PK
duel_session_id     UUID FK → DuelSession
mission_id          UUID FK → Mission
attacker_user_id    UUID FK → User
sabotage_type       SabotageType
target_scope        SabotageScope (CHEFE, MEMBRO_ESPECIFICO, EQUIPE, SILENCIOSO)
target_user_id      UUID FK → User (nullable; usado quando scope = MEMBRO_ESPECIFICO)
applied_at          TIMESTAMP
expires_at          TIMESTAMP       -- TTL; NULL = permanente (reservado para casos extremos)
reversed_at         TIMESTAMP (nullable)
effect_payload      JSONB           -- detalhes do efeito (ex: task_id bloqueada)
```

### Novos enums

```java
enum GlobalRole      { ADMIN, HERO, VILLAIN, OPERATOR }
enum MissionMemberRole { CHEFE, HERO_MEMBER, VILLAIN_INTRUDER }
enum DuelStatus      { PENDING, ACTIVE, HERO_WON, VILLAIN_WON, CANCELLED, TIMEOUT }
enum TaskStatus      { PENDING, IN_PROGRESS, AWAITING_PUZZLE, DONE, BLOCKED }
enum MissionCombatState {
    NORMAL, ALERTA_INFILTRACAO, EM_DUELO,
    SABOTADA, DEFENDIDA, SEM_CHEFE, EM_CRISE, COMPROMETIDA
}
enum PuzzleType      { DRAG_SORT, NODE_CONNECT, SEQUENCE_INPUT }
enum SabotageType    { BLOCK_TASK, SLOW_PROGRESS, REMOVE_MEMBER, SILENT_OBSERVE }
enum SabotageScope   { CHEFE, MEMBRO_ESPECIFICO, EQUIPE, SILENCIOSO }
```

### Casos de uso (application/port/in)

- `CreatePlayerMissionUseCase`
- `InviteMissionMemberUseCase`
- `AssignMissionTaskUseCase`
- `StartInfiltrationUseCase`
- `JoinDuelUseCase`
- `SubmitPuzzleProgressUseCase`
- `ResolveDuelOutcomeUseCase`
- `ApplySabotageUseCase`
- `ListVillainTargetsUseCase`
- `ElectNewMissionChiefUseCase`
- `CloseMissionUseCase`

### Políticas de autorização

- herói cria missão se `GlobalRole == HERO`;
- apenas `CHEFE` da missão gerencia membros e tarefas;
- vilão só inicia infiltração se `GlobalRole == VILLAIN` e não está em cooldown;
- `VILLAIN_INTRUDER` lê dados da missão e escolhe vetor de sabotagem; não altera dados permanentes;
- leitura de dados sensíveis da missão (membros, tarefas) restrita a membros ativos;
- `GlobalRole` carregado no JWT como claim; validado pelo Spring Security em cada request;
- `MissionMemberRole` validado na camada de aplicação (use case), não no filtro HTTP.

### Eventos Kafka (domínio e auditoria)

```
mission.created.by.hero
mission.member.invited
mission.member.joined
mission.task.assigned
mission.task.completed
mission.closed
infiltration.started
duel.created
duel.round.progress
duel.round.finished
duel.finished
sabotage.applied
sabotage.expired
mission.defended
mission.chief.changed
```

### Canais WebSocket (duelo em tempo real)

Tópicos STOMP:

```
/topic/duel/{duelId}           -- broadcast para ambos os participantes
/user/queue/duel-invite        -- convite de duelo para defensor específico
/user/queue/errors             -- erros de validação de move
```

Mensagens publicadas pelo servidor:

```
duel:started     { duelId, seed, puzzleType, roundMax, timeoutAt }
duel:progress    { userId, roundNumber, percentComplete }
duel:round-end   { roundNumber, winnerId }
duel:ended       { winnerId, outcome: "DEFENDED" | "SABOTAGED" }
duel:cancelled   { reason }
duel:timeout-warning { secondsLeft }
```

### Canais SSE (eventos de missão)

```
GET /sse/missions/{id}/events
```

Eventos emitidos:

```
mission:member-invited
mission:task-assigned
mission:task-done
mission:infiltration-alert
mission:state-changed
mission:sabotage-active
mission:chief-changed
```

### Estratégia de migration de banco

Usar Flyway (se já no projeto) ou Liquibase. Cada entidade nova recebe um arquivo de migration separado, na ordem:

1. `V{n}__add_mission_member.sql`
2. `V{n+1}__add_mission_task.sql`
3. `V{n+2}__add_duel_session.sql`
4. `V{n+3}__add_puzzle_attempt.sql`
5. `V{n+4}__add_sabotage_event.sql`
6. `V{n+5}__add_user_cooldown.sql`

---

## Frontend (React + Vite)

## Princípio obrigatório: 100% padrão visual atual

Toda tela nova deve reutilizar os padrões já implementados:

- `AppShell` (admin/hero/villain) como estrutura base;
- `PageHeader`, `SectionCard`, `StatCard`, `EmptyState`, `LoadingState`;
- badges existentes e tokenização de cores em `global.css`;
- linguagem visual do redesign N2 (tipografia, contraste, espaçamento, hierarquia).

### Proibições explícitas

- não criar design paralelo fora do sistema de componentes;
- não introduzir biblioteca de UI que conflite com o estilo atual;
- não usar paleta fora dos tokens definidos;
- não criar telas sem estados de loading/erro/vazio.

### Novas páginas e rotas

| Página | Rota | Guard |
|---|---|---|
| `VillainOpsPage` | `/villain/ops` | `GlobalRole == VILLAIN` |
| `MissionCommandPage` | `/missions/:id/command` | membro da missão |
| `DuelArenaPage` | `/duels/:id/arena` | participante do duelo |
| `MissionTasksPage` | `/missions/:id/tasks` | membro da missão |

Rotas protegidas por guard de role no React Router; tentativa de acesso indevido redireciona para `/403`.

### Componentes novos previstos

- `DuelStatusCard` — estado atual do duelo, rounds, tempo restante
- `PuzzleTimer` — countdown com aviso visual nos últimos 10 s
- `OpponentProgressBar` — progresso do oponente via WS em tempo real
- `TaskAssignmentBoard` — drag-and-drop de tarefas para membros
- `SabotageBanner` — aviso persistente de sabotagem ativa com TTL visível
- `DuelInviteModal` — modal que surge para o defensor quando infiltração inicia
- `CooldownIndicator` — exibe tempo restante de cooldown em contextos relevantes

Todos devem seguir convenções de nomenclatura, CSS modules e composição já usadas no frontend.

### Onboarding e seleção de papel

- Na criação de conta, após registro, usuário é direcionado para tela de onboarding com escolha de `HERO` ou `VILLAIN`.
- `OPERATOR` não aparece no onboarding; é papel atribuído apenas por `ADMIN`.
- Troca de papel disponível em `/settings/role` com validação de cooldown (24 h) e bloqueio se usuário for chefe de missão ativa.
- Frontend deve verificar `GlobalRole` no JWT e adaptar navegação: heróis veem sidebar com missões; vilões veem sidebar com ops/alvos.

---

## Bibliotecas e dependências

### Frontend

- `@dnd-kit/core` e `@dnd-kit/sortable`: puzzle `DRAG_SORT` e `TaskAssignmentBoard`.
- `@xyflow/react`: puzzle `NODE_CONNECT`.
- `@stomp/stompjs` + `sockjs-client`: WebSocket para duelo em tempo real.
- `zod`: validação de payloads de formulários e respostas de puzzle no cliente.
- `date-fns`: formatação de TTL, cooldown, timestamps (adicionar se não consolidado).

Opcional fase avançada:

- `phaser` + wrapper React para puzzle tipo `ARCADE`.

### Backend

- `spring-boot-starter-websocket`: módulo de duelo.
- sem troca de stack principal; Bean Validation mantida para todos os outros endpoints.

---

## Processos que serão implementados

### Processo A — Cadastro e alinhamento de jogador

1. Usuário cria conta com email/senha.
2. Tela de onboarding: escolhe `HERO` ou `VILLAIN`.
3. Sistema persiste `GlobalRole` no perfil e emite JWT com claim `role`.
4. Navegação e rotas liberadas conforme papel.
5. Troca de papel: disponível após cooldown de 24 h; bloqueada se for chefe de missão ativa.

### Processo B — Missão com chefe e tarefas

1. Herói cria missão → nasce com `MissionCombatState.NORMAL` e `owner_user_id` (chefe).
2. Chefe convida membros → cada aceite cria `MissionMember`.
3. Chefe define tarefas com dependências e marca as críticas.
4. Heróis executam tarefas; tarefas críticas transitam para `AWAITING_PUZZLE` e só concluem após puzzle validado no backend.
5. Timeline de eventos (`mission.*`) registra todas as mudanças para auditoria.

### Processo C — Infiltração e alerta

1. Vilão seleciona missão no painel `/villain/ops` (missões com `MissionCombatState.NORMAL` ou `EM_CRISE`).
2. Vilão confirma início de infiltração → backend verifica cooldown, cria `DuelSession` com status `PENDING`.
3. Backend emite evento SSE `mission:infiltration-alert` para membros da missão.
4. `DuelInviteModal` aparece para o defensor com contador regressivo (3 min).
5. Defensor aceita → `DuelSession` transita para `ACTIVE`; seed enviado via WS para ambos.
6. Defensor não aceita em 3 min → vitória automática do vilão.

### Processo D — Resolução do duelo

1. Ambos os lados resolvem o puzzle; cliente envia movimentos via WS `POST /app/duels/{duelId}/progress`.
2. Backend valida cada movimento contra sequência derivada do seed.
3. Primeiro a completar a sequência válida vence o round.
4. Ao atingir `round_max` rounds, backend arbitra vencedor final.
5. **Se herói vence:** missão → `DEFENDIDA`; vilão → cooldown de 10 min; evento `mission.defended` publicado.
6. **Se vilão vence:** vilão escolhe vetor de ataque via `POST /api/duels/{duelId}/sabotage-choice`; efeito aplicado com TTL; missão → `SABOTADA`; evento `sabotage.applied` publicado.
7. `SabotageEvent.expires_at` processado por job agendado (Spring `@Scheduled`) que reverte o efeito e emite `sabotage.expired`.

### Processo E — Auditoria e replay

- Toda ação relevante gera evento Kafka consumido por serviço de histórico.
- Histórico exposto via `GET /api/missions/{id}/timeline`.
- Eventos alimentam dashboard admin e evidência para demo/apresentação.

---

## API — especificação completa

### Missões

```
POST   /api/player-missions                            criar missão (HERO)
GET    /api/player-missions/{id}                       estado atual (membro)
POST   /api/player-missions/{id}/members/invite        convidar herói (CHEFE)
GET    /api/player-missions/{id}/members               listar membros (membro)
DELETE /api/player-missions/{id}/members/{userId}      remover membro (CHEFE)
POST   /api/player-missions/{id}/tasks                 criar tarefa (CHEFE)
PATCH  /api/player-missions/{id}/tasks/{taskId}        atualizar tarefa (CHEFE/assignee)
GET    /api/player-missions/{id}/tasks                 listar tarefas (membro)
POST   /api/player-missions/{id}/close                 encerrar missão (CHEFE)
GET    /api/player-missions/{id}/timeline              histórico de eventos (membro)
POST   /api/player-missions/{id}/chief/elect           eleger novo chefe (membros; maioria)
```

### Vilão e infiltração

```
GET    /api/villain/targets                            missões infiltráveis (VILLAIN)
POST   /api/missions/{id}/infiltration/start           iniciar infiltração (VILLAIN)
```

### Duelo

```
POST   /api/duels/{duelId}/join                        defensor aceita duelo
GET    /api/duels/{duelId}                             estado atual do duelo (participante)
GET    /api/puzzles/{type}/definition?seed={seed}      definição do puzzle derivada do seed
POST   /api/duels/{duelId}/sabotage-choice             vilão escolhe vetor após vitória
```

### WebSocket (STOMP)

```
/app/duels/{duelId}/progress    cliente envia movimentos
/app/duels/{duelId}/ready       cliente sinaliza pronto para iniciar round
```

### Real-time (SSE)

```
GET    /sse/missions/{id}/events    stream de eventos da missão
```

---

## Regras de negócio essenciais

- uma missão tem exatamente um chefe ativo;
- chefe pode ser substituído por eleição de membros se ficar offline;
- apenas chefe atribui tarefas e altera papéis da equipe;
- vilão não altera dados permanentes da missão sem vencer duelo;
- sabotagem é limitada por tipo, escopo e TTL;
- nenhuma sabotagem apaga histórico ou eventos Kafka;
- apenas um `DuelSession` `PENDING` ou `ACTIVE` por missão por vez;
- movimentos de puzzle são validados pelo backend contra seed; cliente não é confiável;
- três movimentos inválidos consecutivos em um duelo = disqualificação automática do remetente.

---

## Plano por fases (entrega incremental)

## Fase 0 — Decisões de fundação

- registrar e aprovar todas as decisões desta seção antes de qualquer código das fases seguintes;
- criar migrations de banco para todas as entidades novas;
- configurar WebSocket no Spring e cliente STOMP no React;
- validar seed de puzzle com teste de integração cobrindo geração → distribuição → validação.

Critério de aceite:

- migrations aplicadas sem erro em ambiente local;
- teste de seed: mesmo seed produz mesma sequência em Java e JavaScript.

## Fase 1 — Base jogável (obrigatória)

- herói cria missão;
- papel `CHEFE` por missão;
- convite e aceite de heróis;
- tarefas com status básico (sem puzzle ainda);
- telas `MissionCommandPage` e `MissionTasksPage` seguindo AppShell e componentes existentes;
- SSE de eventos de missão funcionando.

Critério de aceite:

- fluxo completo de criação, convite e atribuição sem regressão de telas atuais.

## Fase 2 — Puzzle de tarefa

- adicionar puzzle `DRAG_SORT` para concluir tarefas críticas;
- tarefa crítica transita para `AWAITING_PUZZLE` e só conclui após puzzle validado no backend com seed;
- persistir `PuzzleAttempt` com `time_ms` e `is_valid`.

Critério de aceite:

- tarefa crítica só conclui após puzzle validado no backend;
- tentativas inválidas rejeitadas pelo servidor.

## Fase 3 — Infiltração MVP

- painel `VillainOpsPage` com alvos;
- alerta SSE ao defensor + `DuelInviteModal`;
- duelo best-of-1 via WebSocket com puzzle `DRAG_SORT`;
- desfecho: defesa → cooldown do vilão; sabotagem → `BLOCK_TASK` com TTL;
- job de reversão de sabotagem.

Critério de aceite:

- duelo em tempo real funcional em ambiente local com dois usuários;
- efeito de sabotagem aplicado e revertido automaticamente após TTL.

## Fase 4 — Duelo completo + árvore de ataque

- best-of-3;
- adicionar puzzle `NODE_CONNECT` e `SEQUENCE_INPUT`;
- todos os vetores de `SabotageType` implementados;
- cooldowns completos (tabela `user_cooldown`);
- eleição de novo chefe.

Critério de aceite:

- comportamento previsível e balanceado sob cenários concorrentes;
- todos os fluxos de exceção do duelo cobertos por testes de integração.

## Fase 5 — Observabilidade e polish

- endpoint de timeline por missão;
- dashboard admin com métricas de duelo e sabotagem;
- ranking de vilões e heróis;
- tuning de dificuldade de puzzle;
- refinamento UX/a11y (contraste, foco, estados não só por cor).

Critério de aceite:

- jornada completa estável para demo final;
- checklist de conformidade visual 100% aprovado.

---

## Qualidade, testes e validação

### Backend

Testes de integração obrigatórios para:

- criação de missão por herói; rejeição se `GlobalRole != HERO`;
- autorização por papel em cada endpoint sensível;
- abertura, rounds e encerramento de duelo;
- validação de seed: movimentos corretos passam, inválidos são rejeitados;
- disqualificação após três movimentos inválidos consecutivos;
- aplicação de sabotagem com TTL e reversão pelo job agendado;
- cooldown persiste após reinício da aplicação;
- apenas um duelo ativo por missão (lock).

### Frontend

Testes de componentes e fluxos:

- render de estados loading/erro/vazio em todas as novas páginas;
- guard de rota por `GlobalRole`;
- atualização em tempo real via WS na `DuelArenaPage`;
- `SabotageBanner` aparece e desaparece corretamente conforme TTL.

### Teste manual de aceite

1. Login como herói A (chefe), herói B e vilão.
2. Criar missão, convidar herói B, atribuir tarefa crítica.
3. Herói B completa puzzle da tarefa crítica.
4. Vilão inicia infiltração; herói A recebe alerta.
5. Executar duelo best-of-1; validar efeito da vitória/derrota.
6. Aguardar TTL da sabotagem e confirmar reversão automática.
7. Confirmar timeline e consistência visual.

---

## Conformidade obrigatória com padrão atual do frontend

Antes de merge, toda entrega deve passar pelo checklist:

- usa `AppShell` correto por contexto;
- mantém tokens e tipografia do redesign;
- utiliza componentes existentes sempre que equivalentes;
- respeita copy e hierarquia visual de operações;
- mantém acessibilidade mínima (contraste, foco, estado não só por cor);
- não introduz regressão visual em telas N2;
- toda nova página tem estados de loading, erro e vazio implementados.

Se qualquer item falhar, a implementação é considerada incompleta.

---

## Requisitos não-funcionais

- latência de round-trip do duelo: < 200 ms em rede local;
- suporte a até 20 duelos simultâneos sem degradação (dimensionamento mínimo para demo);
- cooldowns persistidos em banco; nenhum estado volátil em memória;
- nenhum evento de histórico/Kafka pode ser deletado por sabotagem;
- migrations de banco devem ser idempotentes e reversíveis (rollback script).

---

## Riscos e mitigação

| Risco | Mitigação |
|---|---|
| Alta complexidade de tempo real | Duelo MVP best-of-1 na Fase 3 antes do modelo completo |
| Desbalanceamento herói/vilão | Cooldowns, limites por sessão e telemetria de win-rate |
| Quebra de padrão visual por telas novas | Checklist obrigatório + reutilização de componentes |
| Concorrência em múltiplos ataques | Lock pessimista por missão/duelo no backend |
| Cheat via manipulação de sequência no cliente | Seed gerado e validado exclusivamente no backend |
| Chefe offline sem substituto | Fluxo de eleição de novo chefe na Fase 4 |
| Cooldown contornado por reinício de app | Cooldowns persistidos em banco, não em memória |

---

## Resultado esperado

Ao final deste plano, a Central-LJ terá:

- protagonismo real dos jogadores com papéis bem definidos;
- missões com comando, cooperação e conflito auditável;
- vilões com infiltração justa, controlada e à prova de cheat;
- duelos em tempo real com seed verificável e fluxos de exceção cobertos;
- experiência moderna sem perder a identidade visual e técnica já consolidada no projeto.
