# N2 — Redesign do shell, dashboards e identidade visual (UI premium)

## O que foi redesenhado

- **Shell global:** layouts administrativo e do herói passam a usar o componente **`AppShell`**, com **sidebar** vertical (marca, navegação com ícones SVG leves, bloco de sessão com avatar iniciais e saída) e **topbar** com identidade de modo (**Coordenação tática** vs **Operação ativa**).
- **Tokens e fundo:** `global.css` expande paleta (**azul petróleo**, superfícies, **ciano**, **dourado**, vermelho de alerta), raios, sombras e variáveis de shell separando realces **admin** (azul/ciano) e **herói** (violeta operacional).
- **Dashboard admin:** métricas com **`StatCard`** (inclui **Em análise** via API), blocos de **alertas**, **missões prioritárias**, **recursos** (heróis disponíveis / em missão, equipes ativas) e lista operacional dentro de **`SectionCard`**.
- **Área do herói:** **`HeroAreaPage`** carrega **`getHero`** (quando há `heroiId`) e **`listMyMissions`**, exibe faixa de disponibilidade/equipe, métricas, **missão mais urgente** e próximos passos; **`HeroMissionsPage`** com **`PageHeader`**, destaque de linha por prioridade e **`EmptyState`**.
- **Missões:** **`MissionsListPage`** com filtros por status e colunas enriquecidas; **`MissionDetailPage`** com painel principal premium, **`PriorityBadge`**, **`Timeline`** componentizado e atribuição em **`SectionCard`**.
- **Heróis e equipes:** listas em **grade de cards**; detalhe do herói com cabeçalho dourado e missões com **`PriorityBadge`**; detalhe de equipe com módulo CSS próprio e lista de membros formatada.
- **Login:** layout em duas colunas (hero branding + formulário), mensagem de boas-vindas e **caixa de erro** destacada.
- **Formulário nova missão:** alinhado ao **`PageHeader`** e cartão visual consistente.

## Componentes criados ou refinados

| Componente | Notas |
|------------|--------|
| **`AppShell`** | Shell compartilhado; prop `mode: "admin" \| "hero"` altera fundo, borda da sidebar e pill da topbar. |
| **`NavIcons`** | Ícones inline para navegação (sem dependência extra). |
| **`PageHeader`** | Kicker, título, descrição e área de ações. |
| **`SectionCard`** | Título, hint e variantes `default` / `alert` / `gold`. |
| **`StatCard`** | Evolução visual das métricas (barra lateral, tipografia display). |
| **`MetricCard`** | Reexporta / delega a **`StatCard`** (compatibilidade). |
| **`PriorityBadge`** | Badge forte por `PrioridadeMissao`. |
| **`EmptyState`** | Estado vazio com ícone, texto e ação opcional. |
| **`LoadingState`** | Loading com pulso. |
| **`Timeline`** | Histórico de missão reutilizável (origens com cores distintas). |

Componentes já existentes mantidos e integrados: **`StatusBadge`**, **`HeroAvailabilityBadge`**, **`N1DevTools`**, **`RequireAuth`**.

## Diferença entre shell admin e shell herói

- **Admin:** gradientes e realce **azul/ciano** na sidebar; topbar “**Central de operações**”; navegação: Painel, Missões, Heróis, Equipes, Nova missão.
- **Herói:** realce **violeta** na sidebar; topbar “**Área do herói**”; navegação enxuta: Minha área, Minhas missões, Perfil heroico (se houver `heroiId`).
- Ambos compartilham a mesma estrutura (**AppShell**), evitando “dois produtos”, mas com **sinalização visual clara** de contexto.

## Princípios visuais aplicados

- **Desktop first**, com quebras simples para sidebar empilhada &lt; 960px.
- Contraste elevado, hierarquia com **Space Grotesk** em títulos e **Inter** no corpo.
- **Cards** com borda, gradiente sutil e sombra; **urgência** em vermelho/laranja; **dourado** para destaque institucional / prioritário.
- Estados **carregando**, **vazio** e **erro** tratados de forma explícita na jornada principal.

## Validação rápida

1. `npm run build` em `frontend/` (TypeScript + Vite).
2. Login como **ADMIN** e como **HERO** — checar sidebar, topbar e rotas protegidas.
3. Painel: métricas, alertas, prioritárias, recursos, tabela com filtros.
4. Missões: filtros na lista; detalhe com timeline e atribuição (admin).
5. Herói: área inicial com missão urgente; minhas missões; ficha com volta correta para “Minha área” quando for o próprio perfil.
