# Transição da N1 para a N2 (Central-LJ)

Este documento fixa o **estado após a N1**, o que **ainda não existe**, os **objetivos da N2**, o que **já pode ser reaproveitado** e uma **ordem sugerida** de implementação — para evitar escopo creep e discussões ambíguas na equipe ou na banca.

---

## 1. Estado atual (o que a N1 concluiu)

### Artefatos
- Documentação em `docs/n1/` (visão, RNFs, testes, padrões, roteiros).
- Diagramas PlantUML: C4 Contexto, C4 Contêiner, diagrama de classes de domínio.
- Material Figma: `docs/figma/style-guide.md` e `link-figma.md` (com link a preencher pela equipe).
- Pacote de entrega: `docs/entrega/` (checklist, resumo, artefatos, ordem de apresentação).

### Código e infraestrutura
- **Frontend:** página de validação com health, hello, missão de teste (HTTP) e publicação de evento de teste (via API).
- **Backend:** endpoints `GET /api/health`, `GET /api/hello`, `POST /api/missions/test`, `POST /api/events/publish-test`; produtor Kafka; consumidor mínimo (evidência em log); CORS e tratamento de erro coerentes com dev local.
- **Infra:** Docker Compose com Kafka, Kafka UI e PostgreSQL; scripts de subida/descida/reset.

### Mensageria
- Kafka comprovado como **canal de eventos** na N1 (publicação explícita de teste + consumo em log). **Não** há ainda encadeamento completo “criar missão → série de eventos de negócio → estado persistido”.

---

## 2. Pendências (o que a N1 deliberadamente não fechou)

| Área | Pendência |
|------|-----------|
| Persistência | Modelo JPA/JDBC completo; migrações; tabelas de missão, histórico, usuários conforme disciplina. |
| Fluxo de negócio | Transições reais de `StatusMissao`; consumidores que **alterem** estado ou disparem efeitos. |
| Segurança | Autenticação e autorização; papéis (operador/admin); endurecimento de CORS para além do dev. |
| Interface | Telas completas alinhadas ao Figma; timeline alimentada por dados reais; WebSocket/SSE. |
| Testes | Integração API + DB + Kafka (ex.: Testcontainers); E2E opcional. |
| Observabilidade | Métricas/tracing além de health e logs básicos (evolução N2/N3 conforme curso). |

Nenhuma dessas pendências invalida a N1: elas são o **contrato** da próxima etapa.

---

## 3. Objetivos da N2 (proposta de escopo)

1. **Persistir** missões e histórico em **PostgreSQL** (fonte da verdade transacional).
2. **Publicar eventos de domínio** no momento adequado do fluxo (não só endpoint de teste isolado).
3. **Consumidores** que executem regras ou projetem leituras (ex.: atualização de status, notificações internas).
4. **API REST** refletindo recursos reais (listagem, detalhe, transições onde couber).
5. **Segurança mínima exigida** pelo curso (login, perfis ou token, conforme orientação docente).
6. **Testes** ampliados: pelo menos um caminho integrado crítico automatizado.

Objetivos finos (prioridade exata) devem ser alinhados à ementa e ao cronograma da disciplina.

---

## 4. O que já está pronto para evoluir (sem recomeçar do zero)

| Base N1 | Como evolui na N2 |
|---------|-------------------|
| Estrutura de pacotes backend (`controller`, `dto`, `service`, `messaging`) | Acrescentar `domain` entidades, `domain` serviços, `repository` Spring Data |
| `MissionEvent` / produtor | Enriquecer payload e versionamento de eventos; publicar após commits ou use cases |
| Consumer atual | Substituir/estender para idempotência, tratamento de erro e atualização de persistência |
| Frontend serviços e tipos | Novas chamadas para CRUD/listagem quando API existir |
| Compose Postgres | Configurar `spring.datasource`, Flyway/Liquibase se adotado |
| Diagramas | Atualizar apenas o que mudar (ex.: novos contêineres só se surgirem) |

---

## 5. Prioridades sugeridas na N2 (ordem de implementação)

Ordem que reduz risco técnico e sustenta demonstrações crescentes:

1. **Modelo de dados + migrações** — tabelas `missao`, enums/status, histórico opcional.  
2. **Persistência de “criar missão”** — substituir ou complementar o fluxo apenas-HTTP de teste por persistência real (mantendo endpoint de teste apenas se a disciplina permitir).  
3. **Evento ao criar/atualizar** — publicar no Kafka quando o estado mudar no banco (transação + outbox **ou** publicação pós-commit, conforme decisão da equipe).  
4. **Consumidor com efeito** — ao menos um consumidor que valide mensagem e atualize status ou registre processamento.  
5. **API de leitura** — listar/detalhar missão para o React consumir.  
6. **Autenticação** — proteger endpoints sensíveis.  
7. **UI** — listagem e detalhe; timeline com dados reais ou semi-reais.  
8. **WebSocket/SSE** — se couber no prazo, após leitura consistente via API.

**Observação:** a ordem pode ser ajustada se o professor priorizar auth antes de Kafka completo; o importante é **documentar** a decisão no repositório.

---

## 6. Riscos técnicos (N2)

| Risco | Mitigação |
|-------|-----------|
| Duplicidade de processamento no Kafka | Chaves de partição, idempotência no consumidor, ou padrão outbox |
| Consistência DB ↔ evento | Transações claras; evitar “publicou mas não commitou” |
| Escopo grande demais | Cortar WebSocket antes de estabilizar fluxo core + DB |
| Ambiente dos avaliadores | Manter README e um `resumo-entrega` atualizado; scripts testáveis |

---

## 7. Recomendação final

Trate a **N1 como linha de base congelada**: commits ou tags que marquem “entrega N1” ajudam a separar **documentação + spike técnico** da **implementação N2**. Antes de codificar grandes features, atualize **este arquivo** ou um ADR curto quando a disciplina exigir rastreabilidade de decisões.

Próxima leitura obrigatória para dev: `docs/entrega/resumo-entrega-n1.md` e `docs/n1/03-plano-de-testes.md` (seção estratégia N2).
