# N2 — Atribuição de missões (herói ou equipe)

## Comportamento de produto (MVP)

Uma missão pode ter **no máximo um responsável explícito** nesta etapa:

- **Herói** designado **ou**
- **Equipe** designada.

Ao designar um, o outro vínculo é **limpo** (atribuição exclusiva, fácil de explicar em demo). O campo `atribuidoPor` aceita texto livre (ex.: “Coordenação”, nome de operador); se vazio, o backend usa um valor padrão configurado no serviço.

## Backend

### Endpoints

| Método | Caminho | Corpo (JSON) |
|--------|---------|--------------|
| PATCH | `/api/missions/{id}/assign-hero` | `{ "heroiId": "uuid", "atribuidoPor": "opcional" }` |
| PATCH | `/api/missions/{id}/assign-team` | `{ "equipeId": "uuid", "atribuidoPor": "opcional" }` |

Implementação: `MissionAssignmentService` (transacional).

### Efeitos no domínio

1. Carrega a missão com relacion carregado (`findByIdWithAssignments`).
2. Valida herói ativo ou equipe ativa.
3. **Libera** o herói anterior se estava `EM_MISSAO` (volta para `DISPONIVEL` quando aplicável).
4. Atualiza FK da missão, `atribuidoEm`, `atribuidoPor`, `ultimaAtualizacao`.
5. Se o novo responsável é um herói **DISPONIVEL**, passa a **EM_MISSAO**.
6. Grava no **histórico** (`MissionHistory`) uma entrada com origem **`API_ATRIBUICAO`** e mensagem narrativa:
   - `Herói "…" designado à missão.`
   - `Equipe "…" designada à missão.`
7. Notifica clientes via **`MissionRealtimeNotifier`** (SSE / painel), sem alterar o fluxo Kafka da criação da missão.

O **status da missão** (enum de workflow) permanece o que o pipeline Kafka já definiu; a atribuição manual **não força** novo passo de workflow — apenas enriquece contexto e auditoria. Mensagens de histórico podem repetir o mesmo status em `statusAnterior` e `statusNovo` quando a mudança é só de designação.

### Resposta e detalhe

`MissionResponse` / detalhe incluem o bloco `atribuicao`:

- `heroiId`, `nomeHeroico`, `equipeId`, `nomeEquipe`, `atribuidoEm`, `atribuidoPor`

Montado por `MissionMapper.buildAtribuicao`.

## Frontend

- Na **tela de detalhe da missão** (`/missoes/:id`): painel **Atribuição** com selects de heróis ativos e equipes ativas, campo opcional “Registrado por”, e exibição da designação atual (com links para `/herois/:id` e `/equipes/:id` quando houver id).
- Na **linha do tempo**, entradas com origem **`API_ATRIBUICAO`** usam estilo visual distinto (roxo) para diferenciar de `API_REGISTRO` e eventos Kafka.

## Testes automatizados

`HeroEquipeAssignmentApiIntegrationTest` (MockMvc, producer mockado):

- criar equipe e herói;
- criar missão;
- atribuir herói: resposta, detalhe com `atribuicao`, segundo item do histórico com `API_ATRIBUICAO`, `GET /api/heroes/{id}/missions` retorna a missão;
- atribuir equipe: resposta e histórico equivalentes.
