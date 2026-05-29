// Central-LJ — Documentação Técnica
// PDF: ./scripts/build-documentacao-pdf.sh

#let brand-dark  = rgb("#0d1220")
#let brand-navy  = rgb("#1a2744")
#let brand-blue  = rgb("#2d5a8a")
#let brand-accent = rgb("#00b894")
#let brand-muted = rgb("#5c6b82")
#let brand-bg    = rgb("#f4f7fb")
#let brand-line  = rgb("#d8e0ec")

#set document(
  title: "Central-LJ — Documentação Técnica",
  author: "Projeto Integrador de Módulo — PUC Goiás",
)

#set page(
  paper: "a4",
  margin: (x: 1.9cm, top: 2.1cm, bottom: 2cm),
  numbering: "1",
  fill: white,
  header: context {
    let pg = counter(page).get().first()
    if pg > 2 [
      #set text(size: 7.5pt, fill: brand-muted)
      #grid(
        columns: (1fr, auto, 1fr),
        align: (left, center, right),
        gutter: 8pt,
        [#text(fill: brand-navy, weight: "semibold")[Central-LJ]],
        [#line(length: 100%, stroke: 0.4pt + brand-line)],
        [Documentação Técnica · PUC Goiás 2026/1],
      )
    ]
  },
  footer: context {
    let pg = counter(page).get().first()
    if pg > 2 [
      #set text(size: 8pt, fill: brand-muted)
      #grid(
        columns: (1fr, auto, 1fr),
        align: (left, center, right),
        [#text(fill: brand-blue)[●] #text(fill: brand-accent)[●]],
        [#counter(page).display("1")],
        [ADS · Projeto Integrador],
      )
    ]
  },
)

#set text(lang: "pt", size: 9.8pt, font: "Libertinus Serif", hyphenate: true)
#set par(justify: true, leading: 0.58em, spacing: 0.55em, first-line-indent: 0pt)
#set list(indent: 1.2em, spacing: 0.35em)
#set heading(numbering: "1.1")
#set figure(gap: 0.35em, supplement: [Fig.])

#show heading.where(level: 1): it => {
  v(0.35em, weak: true)
  block(breakable: false, below: 0.55em)[
    #grid(
      columns: (auto, 1fr),
      column-gutter: 10pt,
      align: (center + horizon, bottom),
      box(
        fill: brand-navy,
        inset: (x: 8pt, y: 5pt),
        radius: 3pt,
      )[
        #set text(size: 8pt, fill: white, weight: "bold")
        #counter(heading).display()
      ],
      text(size: 15pt, weight: "bold", fill: brand-navy)[#it.body],
    )
    #v(0.25em)
    #grid(
      columns: (3pt, 1fr),
      gutter: 4pt,
      rect(width: 3pt, height: 0.9em, fill: brand-accent),
      line(length: 100%, stroke: 0.5pt + brand-line),
    )
  ]
}

#show heading.where(level: 2): it => {
  v(0.45em, weak: true)
  block(below: 0.35em)[
    #text(size: 11pt, weight: "bold", fill: brand-blue)[#it.body]
  ]
}

#show heading.where(level: 3): it => {
  v(0.3em, weak: true)
  block(below: 0.25em)[
    #text(size: 10pt, weight: "semibold", fill: brand-navy)[#it.body]
  ]
}

#set table(
  stroke: 0.45pt + brand-line,
  inset: (x: 6pt, y: 5pt),
  fill: (x, y) => if y == 0 { brand-navy } else if calc.rem(y, 2) == 0 { brand-bg } else { white },
)
#show table.cell.where(y: 0): set text(fill: white, weight: "bold", size: 8.5pt)
#show table.cell: set text(size: 8.5pt)

#show outline.entry.where(level: 1): it => {
  v(0.2em, weak: true)
  strong(it)
}

#let callout(body, kind: "info") = {
  let (fill, stroke, icon) = if kind == "warn" {
    (rgb("#fff8eb"), rgb("#d4a017"), "⚠")
  } else {
    (rgb("#eef5ff"), brand-blue, "◆")
  }
  block(
    fill: fill,
    stroke: (left: 2.5pt + stroke),
    inset: (left: 10pt, rest: 8pt),
    radius: 2pt,
    width: 100%,
    above: 0.35em,
    below: 0.35em,
  )[
    #set par(spacing: 0.4em)
    #text(fill: stroke, weight: "bold")[#icon] #body
  ]
}

#let codeblock(body) = block(
  fill: rgb("#f0f3f8"),
  stroke: 0.45pt + brand-line,
  radius: 3pt,
  inset: 8pt,
  width: 100%,
  above: 0.3em,
  below: 0.3em,
)[
  #set text(size: 7.8pt)
  #set par(leading: 0.5em, spacing: 0.35em, justify: false)
  #body
]

#let stat-card(value, label) = box(
  width: 100%,
  fill: brand-bg,
  stroke: 0.5pt + brand-line,
  radius: 4pt,
  inset: 8pt,
)[
  #align(center)[
    #text(size: 18pt, weight: "bold", fill: brand-accent)[#value]
    #v(0.15em)
    #text(size: 8pt, fill: brand-muted)[#label]
  ]
]

#let diagram(path, caption, label-name, height: none) = {
  let img = if height != none {
    image(path, width: 100%, height: height, fit: "contain")
  } else {
    image(path, width: 100%, fit: "contain")
  }
  [
    #figure(
      block(
        fill: white,
        stroke: 0.6pt + brand-line,
        radius: 4pt,
        inset: 8pt,
        width: 100%,
      )[#align(center)[#img]],
      caption: caption,
      kind: "figure",
      placement: none,
    )
    #label(label-name)
  ]
}

#let diagram-landscape(path, caption, label-name) = {
  pagebreak(weak: true)
  page(flipped: true, margin: 1.5cm)[
    #figure(
      block(
        fill: white,
        stroke: 0.6pt + brand-line,
        radius: 4pt,
        inset: 8pt,
        width: 100%,
      )[
        #align(center)[#image(path, width: 100%, fit: "contain")]
      ],
      caption: caption,
      kind: "figure",
      placement: none,
    )
    #label(label-name)
  ]
}

#let badge(content, tone: "blue") = {
  let fill = if tone == "green" { rgb("#e6f7f2") }
    else if tone == "red" { rgb("#fdecef") }
    else if tone == "yellow" { rgb("#fff8e6") }
    else { rgb("#eef3fb") }
  let color = if tone == "green" { brand-accent }
    else if tone == "red" { rgb("#c0392b") }
    else if tone == "yellow" { rgb("#b8860b") }
    else { brand-blue }
  box(
    fill: fill,
    inset: (x: 5pt, y: 2pt),
    radius: 2pt,
    stroke: 0.4pt + color.lighten(40%),
  )[
    #set text(size: 7.5pt, weight: "semibold", fill: color)
    #content
  ]
}

// ═══════════════════════════════════════
// CAPA
// ═══════════════════════════════════════
#page(margin: 0pt, fill: brand-dark, numbering: none)[
  #place(top + left, rect(width: 100%, height: 100%, fill: gradient.linear(
    brand-dark, brand-navy, angle: 160deg,
  )))
  #place(top + right, dx: 2cm, dy: -1cm, circle(radius: 7cm, fill: brand-accent.transparentize(88%)))
  #place(bottom + left, dx: -2cm, dy: 1cm, circle(radius: 6cm, fill: brand-blue.transparentize(90%)))
  // grid sutil
  #place(top + left)[
    #for y in range(0, 30) [
      #place(top + left, dy: y * 28pt)[
        #line(length: 100%, stroke: 0.25pt + white.transparentize(92%))
      ]
    ]
    #for x in range(0, 22) [
      #place(top + left, dx: x * 28pt)[
        #line(angle: 90deg, length: 100%, stroke: 0.25pt + white.transparentize(92%))
      ]
    ]
  ]
  #place(top + left, dx: 0pt, dy: 0pt)[
    #rect(width: 100%, height: 5pt, fill: brand-accent)
  ]

  #align(left)[
    #pad(left: 2.2cm, top: 1.6cm, right: 2.2cm, bottom: 1.8cm)[
      #set text(fill: white)

      #text(size: 8.5pt, fill: white.transparentize(35%), tracking: 0.12em)[
        PONTIFÍCIA UNIVERSIDADE CATÓLICA DE GOIÁS
      ]
      #v(0.2em)
      #text(size: 8.5pt, fill: white.transparentize(35%), tracking: 0.08em)[
        CURSO DE ANÁLISE E DESENVOLVIMENTO DE SISTEMAS
      ]

      #v(2.8cm)

      #text(size: 9pt, fill: brand-accent, tracking: 0.18em)[
        CENTRAL DE MISSÕES DA LIGA DA JUSTIÇA
      ]
      #v(0.5em)
      #text(size: 46pt, weight: "bold")[
        #text(fill: white)[Central]
        #text(fill: brand-accent)[-LJ]
      ]
      #v(0.35em)
      #line(length: 55%, stroke: 1.2pt + brand-accent)
      #v(0.6em)
      #text(size: 13pt, fill: white.transparentize(15%))[
        Documentação Técnica do Sistema
      ]
      #v(0.35em)
      #text(size: 10pt, fill: white.transparentize(35%))[
        Full-Stack · Event-Driven · Arquitetura Hexagonal
      ]

      #v(1.6cm)

      #grid(
        columns: (1fr, 1fr, 1fr, 1fr),
        gutter: 8pt,
        box(fill: white.transparentize(92%), inset: 10pt, radius: 4pt, width: 100%)[
          #text(size: 7pt, fill: brand-accent, tracking: 0.1em)[PROJETO]
          #v(0.2em)
          #text(size: 8.5pt, fill: white)[Projeto Integrador de Módulo]
        ],
        box(fill: white.transparentize(92%), inset: 10pt, radius: 4pt, width: 100%)[
          #text(size: 7pt, fill: brand-accent, tracking: 0.1em)[DISCIPLINAS]
          #v(0.2em)
          #text(size: 8.5pt, fill: white)[Design · Web · Mensageria · Qualidade]
        ],
        box(fill: white.transparentize(92%), inset: 10pt, radius: 4pt, width: 100%)[
          #text(size: 7pt, fill: brand-accent, tracking: 0.1em)[ENTREGA]
          #v(0.2em)
          #text(size: 8.5pt, fill: white)[N1 + N2]
        ],
        box(fill: white.transparentize(92%), inset: 10pt, radius: 4pt, width: 100%)[
          #text(size: 7pt, fill: brand-accent, tracking: 0.1em)[BANCA]
          #v(0.2em)
          #text(size: 8.5pt, fill: white)[03 / 06 / 2026]
        ],
      )

      #v(0.8cm)

      #box(fill: white.transparentize(94%), inset: 10pt, radius: 4pt, width: 100%)[
        #set align(center)
        #text(size: 8pt, fill: white.transparentize(30%))[
          Spring Boot 3.4 · React 19 · Kafka KRaft · PostgreSQL 16 · JWT · SSE
        ]
      ]

      #v(1fr)

      #grid(
        columns: (1fr, auto),
        align: (left, right),
        text(size: 9pt, fill: white.transparentize(40%))[Goiânia — maio de 2026],
        box(
          fill: brand-accent,
          inset: (x: 14pt, y: 6pt),
          radius: 3pt,
        )[
          #text(size: 8pt, weight: "bold", fill: brand-dark)[DOCUMENTO TÉCNICO v1.0]
        ],
      )
    ]
  ]
]

// ═══════════════════════════════════════
// SUMÁRIO
// ═══════════════════════════════════════
#page(numbering: none)[
  #v(0.5cm)
  #text(size: 20pt, weight: "bold", fill: brand-navy)[Sumário]
  #v(0.15em)
  #grid(
    columns: (4pt, 1fr),
    gutter: 6pt,
    rect(width: 4pt, height: 1.2em, fill: brand-accent),
    line(length: 100%, stroke: 0.5pt + brand-line),
  )
  #v(0.8em)
  #outline(depth: 2, indent: 1.2em)
]

#pagebreak()
#set page(numbering: "1")
#counter(page).update(1)

// ═══════════════════════════════════════
// 01 — VISÃO GERAL
// ═══════════════════════════════════════
= Visão Geral e Contextualização

A *Central-LJ* simula a central de operações da *Liga da Justiça*: plataforma full-stack orientada a eventos (N1), com autenticação JWT, papéis distintos e área do herói (N2). O processamento assíncrono usa *Apache Kafka*; a UI recebe atualizações via *SSE*.

#callout[
  *Escopo de entrega:* N1 (eventos e workflow Kafka) + N2 (auth, herói, atribuição, arquitetura hexagonal). A API registra a missão, publica no Kafka e retorna imediatamente; o workflow roda em background.
]

#grid(
  columns: (1fr, 1fr, 1fr),
  gutter: 8pt,
  stat-card([5], [Entidades de domínio (N1/N2)]),
  stat-card([20+], [Endpoints REST]),
  stat-card([25], [Testes automatizados]),
)

== Problema resolvido

Centrais de operações não podem bloquear o operador. A arquitetura orientada a eventos desacopla *registro* e *processamento*: confirmação imediata na API, priorização e escalonamento em background.

== Escopo funcional

#table(
  columns: (1.1fr, 2.3fr, 0.9fr),
  table.header([*Módulo*], [*Funcionalidades*], [*Perfil*]),
  [Missões], [CRUD, workflow Kafka, atribuição herói/equipe, histórico], [#badge(tone: "blue")[ADMIN/OP]],
  [Heróis e Equipes], [Cadastro, disponibilidade, vínculos], [#badge(tone: "blue")[ADMIN/OP]],
  [Área do Herói], [Missões designadas, timeline, perfil], [#badge(tone: "green")[HERO]],
  [Autenticação], [Login JWT, seed demo, controle de papéis], [#badge(tone: "yellow")[Público]],
  [Dashboard], [KPIs, volume por status, missões recentes], [#badge(tone: "blue")[ADMIN]],
)

== Fluxo principal

#table(
  columns: (0.4fr, 1.1fr, 1.8fr),
  table.header([*Et.*], [*Ação*], [*Resultado*]),
  [1], [`POST /api/missions`], [Requisição recebida],
  [2], [Persistência JPA], [`RECEBIDA` + histórico],
  [3], [After-commit → Kafka], [`missions.created`],
  [4], [Consumer + Strategy], [Transições de status],
  [5], [SSE `mission-update`], [Dashboard atualizado],
)

// ═══════════════════════════════════════
// 02 — ARQUITETURA
// ═══════════════════════════════════════
= Arquitetura do Sistema

== Arquitetura Hexagonal

O backend isola o domínio de Spring, JPA, Kafka e HTTP via *Ports & Adapters*.

#table(
  columns: (1fr, 3.2fr),
  table.header([*Camada*], [*Conteúdo*]),
  [Adapters IN], [6 controllers REST · 2 consumers Kafka · JWT e policies],
  [Application], [10+ use cases · 10+ services · Strategy · commands/views],
  [Domain], [5 entidades · enums — POJOs puros (escopo N1/N2)],
  [Adapters OUT], [5 JPA adapters · produtor Kafka · SSE],
)

#callout[
  *Benefício:* cada adaptador é substituível. Testes mockam ports sem subir Kafka, servlet ou PostgreSQL.
]

=== Estrutura de pacotes

#codeblock[
```
br.edu.central.centrallj/
├── adapter/in/web|messaging/     ← REST, DTOs, JWT, consumers
├── adapter/out/persistence|messaging|realtime/
├── application/port/in|out/      ← Use cases e ports
├── application/service/workflow/ ← Strategy de prioridade
└── domain/                       ← Entidades e enums puros
```
]

// ═══════════════════════════════════════
// 03 — DIAGRAMAS
// ═══════════════════════════════════════
= Diagramas de Arquitetura

Diagramas formais com legenda numerada. Figuras compactas maximizam legibilidade sem desperdiçar páginas.

== C4 — Contexto

#diagram(
  "arquitetura/c4-contexto.png",
  [C4 Nível 1 — atores ADMIN, OPERATOR e HERO; sistemas Kafka e PostgreSQL.],
  "fig-c4-contexto",
  height: 9.5cm,
)

#table(
  columns: (0.9fr, 0.6fr, 2fr),
  table.header([*Elemento*], [*Tipo*], [*Relação*]),
  [ADMIN / OPERATOR], [Pessoa], [Coordena missões, dashboard, elenco heroico],
  [HERO], [Pessoa], [Acompanha missões designadas em `/heroi/area`],
  [SPA React], [Software], [REST + SSE · layouts admin e herói],
  [Spring Boot API], [Software], [Regras, JWT, Kafka, JPA],
  [Kafka / PostgreSQL], [Software], [Eventos + persistência relacional],
)

== C4 — Contêineres

#diagram(
  "arquitetura/c4-container.png",
  [C4 Nível 2 — SPA, API, Kafka KRaft, PostgreSQL e Kafka UI (:8088).],
  "fig-c4-container",
  height: 9.5cm,
)

#table(
  columns: (0.9fr, 1fr, 2.5fr),
  table.header([*Contêiner*], [*Stack*], [*Papel*]),
  [SPA], [React 19 · Vite 6], [Painel admin + área do herói · SSE + polling],
  [API], [Spring Boot 3.4], [REST · Security · Flyway · Kafka in/out],
  [Kafka], [KRaft :9092], [Workflow assíncrono `missions.created`],
  [PostgreSQL], [:5433], [Missões, heróis, equipes, usuários, histórico],
)

== Classes — Domínio

#diagram-landscape(
  "arquitetura/diagrama-classes.png",
  [Entidades de domínio N1/N2 — Missão, Herói, Equipe, Usuário e histórico auditável.],
  "fig-classes",
)

== Pipeline Kafka

#diagram(
  "arquitetura/kafka-pipeline.png",
  [Fluxo `missions.created`: API → after-commit → consumer → workflow → SSE.],
  "fig-kafka-pipeline",
  height: 10.5cm,
)

#table(
  columns: (1.3fr, 2.5fr),
  table.header([*Componente*], [*Responsabilidade*]),
  [`MissionCommandService`], [Persiste `RECEBIDA`; dispara evento interno],
  [`AfterCommitMissionDispatch`], [Publica só após commit JPA],
  [`MissionCreatedEventFactory`], [Invariantes: eventId, missionId, type],
  [`MissionCreatedEventProducer`], [JSON → tópico `missions.created`],
  [`MissionWorkflowService`], [Strategy por prioridade + histórico],
  [`SseMissionNotificationAdapter`], [Push `mission-update`],
)

== Entidade-Relacionamento

#diagram(
  "arquitetura/er-banco.png",
  [Modelo relacional PostgreSQL 16 — núcleo N1/N2 (missões, elenco, auth, histórico).],
  "fig-er",
  height: 11cm,
)

// ═══════════════════════════════════════
// 04 — DOMÍNIO
// ═══════════════════════════════════════
= Modelo de Domínio

== Entidades principais

#table(
  columns: (0.9fr, 1.5fr, 1.4fr),
  table.header([*Entidade*], [*Atributos-chave*], [*Papel*]),
  [`Mission`], [prioridade, status, local, heroiId, equipeId], [Missão operacional],
  [`MissionHistory`], [statusAnterior/Novo, origem, ocorridoEm], [Auditoria imutável],
  [`Heroi`], [nome, disponibilidade, equipeId], [Elenco em campo],
  [`EquipeHeroica`], [nome, descricao], [Agrupamento de heróis],
  [`Usuario`], [email, senhaHash, role, heroiId], [Auth JWT e papéis globais],
)

== Máquina de estado — MissionStatus

#box(stroke: 0.5pt + brand-line, inset: 8pt, radius: 3pt, fill: brand-bg, width: 100%)[
  `RECEBIDA` → `EM_ANALISE` → `PRIORIZADA` → `EQUIPE_DESIGNADA` → `EM_ANDAMENTO` → `CONCLUIDA` \
  #text(size: 8pt)[
    Missões *CRÍTICA*: fast-track via `CriticalPriorityMissionProcessingFlowStrategy`. \
    Falha no pipeline: `FALHA_PROCESSAMENTO`.
  ]
]

// ═══════════════════════════════════════
// 05–06 — SOLID + PADRÕES
// ═══════════════════════════════════════
= Princípios SOLID e Padrões de Projeto

#grid(
  columns: (1fr, 1fr),
  gutter: 10pt,
  [
    == Princípios SOLID
    #table(
      columns: (0.5fr, 1.5fr),
      table.header([*P*], [*Aplicação*]),
      [S], [Command / Query / Workflow / History separados],
      [O], [Novas strategies sem alterar consumer],
      [L], [Strategies substituíveis no resolver],
      [I], [Use cases granulares por operação REST],
      [D], [Services → ports, nunca JPA/Kafka],
    )
  ],
  [
    == Padrões de Projeto
    #table(
      columns: (0.9fr, 1.6fr),
      table.header([*Padrão*], [*Onde*]),
      [Strategy], [Workflow por prioridade],
      [Factory], [`MissionCreatedEventFactory`],
      [Repository], [Ports JPA desacoplados],
      [DTO / Record], [DTOs imutáveis na camada web],
      [Observer], [`AfterCommitMissionDispatch`],
      [Chain], [Security: CORS → JWT → AuthZ],
    )
  ],
)

#codeblock[
```java
interface MissionProcessingFlowStrategy {
  List<MissionStatus> getSteps();
  boolean supports(PrioridadeMissao p);
}
// Default → 5 etapas | CriticalPriority → fast-track
```
]

// ═══════════════════════════════════════
// 07 — KAFKA
// ═══════════════════════════════════════
= Mensageria com Apache Kafka

Núcleo do requisito *não-CRUD*: reação a fatos de domínio imutáveis. Ver @fig-kafka-pipeline.

#grid(
  columns: (1fr, 1fr),
  gutter: 10pt,
  [
    == Topologia
    #table(
      columns: (1fr, 1fr),
      table.header([*Tópico*], [*Papel*]),
      [`missions.created`], [Workflow principal],
      [`missions.events`], [Debug / testes],
    )
  ],
  [
    == Perfis
    #table(
      columns: (0.7fr, 1.3fr),
      table.header([*Perfil*], [*Config*]),
      [local/prod], [Kafka :9092 · PG :5433],
      [test], [Consumer off · H2 in-memory],
    )
  ],
)

== Contrato de evento

#codeblock[
```json
{ "eventId": "uuid", "missionId": "uuid", "type": "MISSION_CREATED", "occurredAt": "ISO-8601" }
```
]

// ═══════════════════════════════════════
// 08–10 — TEMPO REAL, SEGURANÇA, FRONTEND
// ═══════════════════════════════════════
= Tempo Real, Segurança e Frontend

#grid(
  columns: (1fr, 1fr),
  gutter: 10pt,
  [
    == SSE (tempo real)
    Unidirecional servidor→cliente. Endpoint `GET /api/missions/stream`, adapter `SseMissionNotificationAdapter` e hook `useMissionUpdates` com polling de fallback (12 s).
  ],
  [
    == JWT e papéis (N2)
    #table(
      columns: (0.7fr, 1.5fr),
      table.header([*Papel*], [*Acesso*]),
      [ADMIN], [Governança completa — missões, dashboard, elenco],
      [OPERATOR], [Criar/listar missões, assign hero/team],
      [HERO], [`/api/me/missions` — missões designadas a ele],
    )
  ],
)

== Rotas frontend

#table(
  columns: (0.9fr, 1.2fr, 0.7fr),
  table.header([*Rota*], [*Página*], [*Perfil*]),
  [`/`], [Dashboard KPIs], [ADMIN],
  [`/missoes`], [Lista e detalhe + timeline], [ADMIN/OP],
  [`/herois`, `/equipes`], [Elenco heroico], [ADMIN/OP],
  [`/heroi/area`], [Área do herói], [HERO],
  [`/login`], [Autenticação JWT], [Público],
)

// ═══════════════════════════════════════
// 11–12 — TESTES + RNF
// ═══════════════════════════════════════
= Qualidade, Testes e RNFs

#grid(
  columns: (1.2fr, 1fr),
  gutter: 10pt,
  [
    == Suítes de teste (25 total)
    #table(
      columns: (1.4fr, 0.4fr),
      table.header([*Suíte*], [*N*]),
      [MissionApiIntegrationTest], [9],
      [HeroEquipeAssignmentApiIntegrationTest], [4],
      [AuthApiIntegrationTest], [3],
      [MissionWorkflowIntegrationTest], [3],
      [Unitários (Ingestion, Strategy, etc.)], [6],
    )
    #callout[*BUILD SUCCESS* — JaCoCo: `backend/target/site/jacoco/index.html`]
  ],
  [
    == Requisitos não-funcionais
    #table(
      columns: (0.8fr, 1.4fr),
      table.header([*Cat.*], [*Atendimento*]),
      [Desempenho], [API < 100 ms — Kafka async],
      [Escalabilidade], [Consumer group horizontal],
      [Segurança], [JWT + `@PreAuthorize`],
      [Testabilidade], [H2 + consumer off em CI],
      [Auditoria], [`mission_history` imutável],
    )
  ],
)

// ═══════════════════════════════════════
// 13 — EXECUÇÃO
// ═══════════════════════════════════════
= Guia de Execução

#grid(
  columns: (1fr, 1fr),
  gutter: 10pt,
  [
    *Pré-requisitos:* Java 21 · Node 20+ · Docker Desktop
    #codeblock[
```bash
cd infra && docker compose up -d
cd backend && ./mvnw spring-boot:run   # :8080
cd frontend && npm i && npm run dev    # :5173
cd backend && ./mvnw test
```
    ]
    *Health:* `GET /api/health` → `{"status":"ok"}`
  ],
  [
    == Credenciais demo
    #table(
      columns: (0.6fr, 1.2fr, 0.7fr),
      table.header([*Papel*], [*E-mail*], [*Senha*]),
      [ADMIN], [`coordenacao@central-lj.demo`], [`Admin@demo2026`],
      [HERO], [`heroi.demo@central-lj.demo`], [`Hero@demo2026`],
    )
    #v(0.3em)
    #table(
      columns: (1fr, 1.2fr),
      table.header([*Variável*], [*Descrição*]),
      [`JWT_SECRET`], [Segredo HS256 (≥ 32 chars)],
      [`KAFKA_BOOTSTRAP_SERVERS`], [Broker Kafka],
      [`DATABASE_URL`], [JDBC PostgreSQL],
    )
  ],
)

#v(1em)
#align(center)[
  #box(
    fill: brand-bg,
    stroke: 0.5pt + brand-line,
    inset: 12pt,
    radius: 4pt,
    width: 80%,
  )[
    #text(size: 10pt, weight: "bold", fill: brand-navy)[Central-LJ — Central de Missões da Liga da Justiça]
    #v(0.2em)
    #text(size: 8.5pt, fill: brand-muted)[
      PUC Goiás · ADS 4º Período · Semestre 2026/1 · Entrega N1 + N2
    ]
  ]
]
