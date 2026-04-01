# N2 — Pendências (após Prompt 5)

## Já entregue nesta etapa

- Histórico persistido e timeline na UI.
- Painel com métricas e tabela de recentes (destaque ALTA/CRÍTICA).
- Rotas **Painel**, **Nova missão**, **Detalhe**.
- **SSE** + polling de backup.
- Testes de API e workflow (ver `04-testes.md`).

## Próximos incrementos sugeridos

| Item | Descrição |
|------|------------|
| Auth | Login, papéis, proteção de endpoints e segregação “meu painel”. |
| WebSocket STOMP | Opcional, se precisar bi-direcional ou rooms por missão. |
| Figma completo | Mais telas, sidebar rica, dark tokens exportados do Figma → CSS. |
| Export / relatório | CSV ou PDF de missões concluídas. |
| Outbox / idempotência | Reforço Kafka + DB em produção. |
| Testcontainers | CI com Postgres + Kafka reais. |
| OpenAPI | Contrato publicado + geração de cliente TS. |

## Não reabrir sem necessidade

- Não substituir Kafka por fila síncrona só para “facilitar demo”.
- Não encher o domínio de entidades sem narrativa de missão (evitar CRUD genérico).
