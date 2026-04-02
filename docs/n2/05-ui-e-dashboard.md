# N2 — UI, dashboards e identidade visual

## Shell e navegação

- **`AppShell`** (modo **admin** ou **hero**): sidebar com marca **Central-LJ**, links com ícones, sessão do usuário e topbar contextual.
- **Administrador:** atalhos para painel, missões, elenco (heróis/equipes) e nova missão.
- **Herói:** foco em **Minha área**, **Minhas missões** e **Perfil heroico** (quando existir vínculo).

## Identidade visual

- Fundo **escuro** com superfícies em **azul petróleo**; acentos **ciano**, **dourado** (prioridade institucional) e **vermelho** (alerta/urgência).
- Tipografia: **Inter** (corpo), **Space Grotesk** (títulos / marca), **IBM Plex Mono** (IDs e metadados) — ver `index.html` e `styles/global.css`.
- Componentes reutilizáveis: **`PageHeader`**, **`SectionCard`**, **`StatCard`**, **`StatusBadge`**, **`PriorityBadge`**, **`Timeline`**, **`EmptyState`**, **`LoadingState`**.

## Dashboard administrativo (`/`)

- **StatCards:** total, **em análise**, em andamento, concluídas, falhas.
- **Alertas:** destaque quando há falhas ou missões recentes ALTA/CRÍTICA.
- **Missões prioritárias:** lista derivada das recentes com prioridade elevada.
- **Recursos:** contagem de heróis disponíveis vs em missão (ativos) e equipes ativas.
- **Lista operacional:** mesmos filtros de status (Recentes, Recebidas, Em análise, etc.) com **PriorityBadge** e SSE/polling via **`useMissionUpdates`**.
- **`N1DevTools`** permanece disponível para a demo técnica.

## Área do herói

- **`/heroi/area`:** cabeçalho com nome heroico (quando a ficha carrega), disponibilidade, equipe, métricas de atribuições, bloco **Missão mais urgente** e orientações.
- **`/heroi/minhas-missoes`:** tabela com hierarquia visual por prioridade; estado vazio amigável.

## Missões, heróis e equipes

- **Lista de missões (`/missoes`):** chips de filtro por status; colunas de ameaça e designação; **`EmptyState`** quando não há linhas.
- **Detalhe (`/missoes/:id`):** painel principal com status, prioridade e tipo de ameaça; atribuição em cartão dedicado; linha do tempo componentizada.
- **Heróis:** grade de **cards** com CTA para ficha; detalhe com blocos **`SectionCard`** e lista de missões com badges.
- **Equipes:** cards com status ativa/inativa; detalhe com membros e disponibilidade.

## Tempo real na UI

- Hook **`useMissionUpdates`:** `EventSource` em `/api/missions/stream`, evento `mission-update`, polling **12s**, refresh ao focar a aba (comportamento mantido).

## Documentação relacionada

- Redesign detalhado: [**11-redesign-ui-shell.md**](11-redesign-ui-shell.md).
- Autenticação e papéis: [**09-autenticacao-e-papeis.md**](09-autenticacao-e-papeis.md).
- Área do herói (regras): [**10-area-do-heroi.md**](10-area-do-heroi.md).
- Guia Figma de referência: `docs/figma/style-guide.md`.

## Lacunas para polimento de banca (honestas)

- Microinterações (focus rings custom, skeletons) podem ser refinadas.
- Tabelas densas poderiam evoluir para virtualização se o volume de dados crescer além da demo.
- Gráficos de tendência só valem se houver requisito explícito de métrica temporal agregada no backend.
