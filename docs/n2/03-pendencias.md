# N2 — Pendências (fechamento)

## Já entregue na N2

- Histórico persistido e timeline na UI (incl. destaque visual **KAFKA_WORKFLOW** e **API_ATRIBUICAO**).
- Painel com métricas, tabela e **filtros por status** (demo).
- Rotas **Painel**, **Missões**, **Heróis**, **Equipes**, **Nova missão**, **Detalhe de missão**, módulos de elenco.
- Domínio **Heroi** + **EquipeHeroica** + **atribuição** opcional na missão (`assign-hero` / `assign-team`), com registro no histórico.
- **Autenticação MVP:** entidade **Usuario**, JWT, papéis **ADMIN** / **HERO** (reserva **OPERATOR**), **`GET /api/me/missions`**, tela **`/login`**, **área do herói** com **minhas missões**, rotas e API protegidas por perfil (ver [09](09-autenticacao-e-papeis.md), [10](10-area-do-heroi.md)).
- **SSE** + polling de backup.
- Mensageria com **ingestão** dedicada + **dispatch pós-commit** (fluxo de criação de missão **inalterado**).
- Testes ampliados (API, workflow, unitários, heróis/equipes/atribuição, **auth**) + **JaCoCo** (ver `04-testes.md`).
- Documentação de **banca** (`06-preparacao-banca.md`, `docs/entrega/*`).
- Documentação de módulo: [07-modulo-herois-equipes.md](07-modulo-herois-equipes.md), [08-atribuicao-de-missoes.md](08-atribuicao-de-missoes.md), [09-autenticacao-e-papeis.md](09-autenticacao-e-papeis.md), [10-area-do-heroi.md](10-area-do-heroi.md).

## Próximos incrementos sugeridos

| Item | Descrição |
|------|------------|
| Operador | Fluxo dedicado **OPERATOR** (telas e permissões mais finas que ADMIN). |
| RBAC avançado | Permissões por recurso, políticas centralizadas, auditoria de ações por usuário. |
| Gestão de usuários | CRUD admin para contas, convites, troca de senha, desativação. |
| `atribuidoPor` | Amarrar texto livre a **id do usuário** logado nas operações de atribuição. |
| Dual experience | Refinar “campo” vs “torre de controle” (onboarding, empty states, notificações). |
| Redesign visual | Aplicar Figma de ponta a ponta, microinterações, densidade informacional. |
| Dashboards estratégicos | KPIs por herói/equipe, carga, tempo médio de resolução. |
| WebSocket STOMP | Opcional, se precisar bi-direcional ou rooms por missão. |
| Export / relatório | CSV ou PDF de missões concluídas. |
| Outbox / idempotência | Reforço Kafka + DB em produção. |
| Testcontainers | CI com Postgres + Kafka reais. |
| OpenAPI | Contrato publicado + geração de cliente TS. |
| Sugestão automática | Estratégia de recomendação herói/equipe por tipo de ameaça e prioridade (backend). |
| E2E | Playwright: login admin/herói → atribuição → minhas missões. |

## Não reabrir sem necessidade

- Não substituir Kafka por fila síncrona só para “facilitar demo”.
- Não encher o domínio de entidades sem narrativa de missão (evitar CRUD genérico).

## Depois deste MVP de autenticação

- Recuperação de senha, MFA, integração institucional (LDAP/OIDC), sessões revogáveis em servidor.
- **Edição completa** de herói/equipe (hoje foco em cadastro e leitura + disponibilidade).
- **Atribuição múltipla** ou papéis na mesma missão (líder / suporte), se o produto exigir.
