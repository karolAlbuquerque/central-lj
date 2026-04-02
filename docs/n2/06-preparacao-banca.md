# N2 — Preparação para banca

## Resumo técnico

A **Central-LJ** registra missões, persiste o ciclo de vida em PostgreSQL, dispara **eventos em Kafka** (`missions.created`) e atualiza a interface por **SSE** + polling. O processamento assíncrono evolui status com **Strategy** (fluxo normal vs prioridade crítica) e grava cada passo em **`mission_history`**.

## Arquitetura final (visão oral)

- **Frontend:** React + Vite — painel, criação, detalhe com timeline.
- **Backend:** Spring Boot — camadas Controller → Service → Repository; mensageria isolada em `messaging/*`.
- **Kafka:** tópico de domínio **`missions.created`** (contrato `MissionCreated*`); tópico auxiliar **`missions.events`** só para teste manual.
- **Dados:** PostgreSQL + Flyway (`missions`, `mission_history`).

## Papel do Kafka (frase para banca)

“A API **não** encadeia o processamento pesado na mesma requisição: ela **comita** a missão, publica **um evento** e o **consumer** assume o pipeline — isso desacopla latência HTTP da orquestração e torna o fluxo **orientado a eventos**.”

## Desacoplamento da mensageria (pontos de defesa)

1. **`MissionCreatedEventIngestionService`** — único lugar que interpreta o JSON do tópico `missions.created` (tipo, `missionId`).
2. **`MissionCreatedConsumer`** — adaptador fino (Kafka → ingestão).
3. **`AfterCommitMissionDispatch`** — publicação Kafka + SSE **somente após commit** da transação de criação.
4. **`MissionWorkflowService`** — orquestra transições de domínio; não conhece detalhes de serialização Kafka.

## Decisões de design relevantes

- **SSE** em vez de WebSocket: canal servidor → cliente suficiente para “missão mudou”.
- **Histórico explícito** em tabela: defensável em auditoria e na timeline.
- **Testes:** H2 no CI local; producer Kafka mockado nos testes de API que não precisam de broker.

## O que mostrar ao vivo (mínimo)

1. Compose **subindo** (Postgres + Kafka) — ou “já subi antes”.
2. **Kafka UI** — mensagem em `missions.created` após criar missão.
3. **Painel** — métricas e tabela; **filtros** por status.
4. **Nova missão** — redireciona ao detalhe; timeline preenchendo após o consumer.
5. **`.\mvnw.cmd test`** (ou relatório JaCoCo em `target/site/jacoco` após o mesmo comando).

## Ordem sugerida da apresentação (15–20 min)

1. Problema: centralizar missões e acompanhar estado.  
2. Arquitetura em uma lâmina: browser → API → DB; API → Kafka → consumer → DB; SSE → browser.  
3. Demo: criar missão → ver evento/consumo → timeline.  
4. Qualidade: camadas, strategy, testes.  
5. Pendências honestas (auth, Testcontainers, Figma pixel-perfect).

## Perguntas prováveis (respostas curtas)

| Pergunta | Resposta |
|----------|----------|
| Por que Kafka? | Assíncrono, reprocessamento, desacoplamento entre “registrar” e “processar”. |
| E se o consumer falhar? | Status **FALHA_PROCESSAMENTO** + histórico; mensagem pode ser reentregue; passos são idempotentes após sair de RECEBIDA. |
| Por que não só CRUD? | O núcleo é o **fluxo** e o **evento**, não a tela de edição. |
| Segurança? | Fora do escopo N2; em produção: auth, TLS, ACLs Kafka. |
| Consistência eventual? | UI pode mostrar RECEBIDA por instantes até o consumer rodar; SSE/polling atualizam. |

## Riscos de demo e plano B

| Risco | Plano B |
|-------|---------|
| Docker não sobe | Perfil **`local`** (H2) + explicar que Kafka fica desligado no `test` |
| Kafka lento | Aumentar `CENTRAL_LJ_WORKFLOW_STEP_DELAY_MS` só para demo ou aceitar delay |
| Porta 5433 ocupada | `docker compose down`, ou mudar `DATABASE_URL` |
| Rede bloqueia SSE | O painel ainda **atualiza por polling** (12s) |

## Material de apoio

- `docs/entrega/resumo-final-n2.md`
- `docs/entrega/checklist-banca-final.md`
- `docs/entrega/roteiro-demo-final.md`
