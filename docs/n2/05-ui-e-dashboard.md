# N2 — UI, dashboard e fidelidade ao Figma

## Telas / áreas

| Rota | Função |
|------|--------|
| **`/`** | **Painel administrativo:** cards (total, em andamento, concluídas, falhas), tabela de missões recentes, destaque visual para **ALTA** / **CRÍTICA**, links para detalhe, ferramentas N1 recolhíveis. |
| **`/operacoes/nova`** | Formulário de criação (alinhado ao fluxo N2). |
| **`/missoes/:id`** | Dados da missão + **linha do tempo** (histórico cronológico, origem API vs Kafka). |

## Identidade visual

- Base em **`docs/figma/style-guide.md`**: fundo escuro (`--bg-deep`, `--surface`), acentos verde/azul/alerta, tipografia **Inter**, hierarquia de cards e bordas.
- Componentes reutilizáveis: **`StatusBadge`**, **`MetricCard`**, **`N1DevTools`**.

## Tempo real na UI

- Hook **`useMissionUpdates`**: abre **EventSource** em `/api/missions/stream`, escuta `mission-update`, faz **polling a cada 12s** e **refresh ao voltar** para a aba (`visibilitychange`).

## Lacunas vs Figma completo

- Ainda não há **login**, **sidebar** de navegação rica nem **dashboard** com gráficos.
- Timeline é **vertical simples** (não há componente Figma pixel-perfect).
- Tokens de cor estão em **`global.css`** (não exportados automaticamente do Figma).

## Próximo passo de UI

- Importar medidas/spacing do arquivo Figma; componentizar **tabela** e **timeline** como na biblioteca do time.
