# Guia de estilos — Central-LJ (Figma + implementação)

Referência **prática** para montar o arquivo Figma e evoluir o React. Objetivo visual: **central de operações de elite** — futurista, tecnológico, **heroico no sentido operacional** (disciplina, urgência, clareza), **profissional**. Evitar estética infantil ou caricata.

---

## Identidade em uma frase

*“Sala de comando escura, dados legíveis, alertas precisos — confiança de quem está no controle.”*

---

## Paleta (tokens — use no Figma como estilos de cor)

| Token | Hex | Onde aplicar |
|--------|-----|----------------|
| `bg-deep` | `#0B1020` | Fundo da aplicação |
| `surface` | `#121A33` | Cards, sidebar, painéis |
| `surface-elevated` | `#1A2540` | Hover de card, dropdown |
| `border` | `#243056` | Bordas e divisórias |
| `text-primary` | `#E6EAF2` | Corpo, títulos de leitura |
| `text-muted` | `#AAB3C5` | Metadados, labels secundários |
| `accent-success` | `#2EE59D` | Sucesso, CTA principal, status “em progresso OK” |
| `accent-danger` | `#FF4D6D` | Crítico, falha, prioridade extrema |
| `accent-priority` | `#FFC857` | Alerta, prioridade alta |
| `accent-info` | `#4DA3FF` | Links, foco, informação neutra |

**Prática:** fundo sempre escuro; **não** usar texto neon contínuo em parágrafos — só em destaques pontuais.

---

## Tipografia (configurar como estilos de texto no Figma)

| Estilo | Fonte sugerida | Tamanho | Uso |
|--------|----------------|---------|-----|
| `heading-xl` | Space Grotesk ou Inter semibold | 32–36 | Marca / hero |
| `heading-lg` | Idem | 24–28 | Título de página |
| `heading-md` | Inter semibold | 18–20 | Seções |
| `body` | Inter regular | 14–16 | Formulários, listas |
| `caption` | Inter regular | 12–13 | Timestamps, IDs |
| `mono` | JetBrains Mono / IBM Plex Mono | 12–13 | Protocolo, JSON, IDs de missão |

**Regra:** no máximo **duas famílias** além da mono de apoio.

---

## Dashboard admin × área do operador (diferença clara)

| Aspeto | **Dashboard admin** | **Área do operador** |
|--------|----------------------|----------------------|
| **Pergunta que responde** | “Como está a central agora?” | “O que eu faço a seguir?” |
| **Densidade** | Alta: vários KPIs, blocos, sparklines | Média: foco em fila e próxima ação |
| **Hierarquia visual** | Comparadores (abertos vs concluídos), alertas agregados | Lista/tabela acionável em primeiro plano |
| **Cores de ênfase** | `accent-danger` em **blocos** (picos), `accent-info` em tendências | `accent-success` / pills de status nas linhas |
| **Navegação** | Entradas para relatórios, visão macro (futuro) | “Nova missão”, “Minhas missões”, detalhe rápido |
| **Tom de copy** | Indicadores, percentuais, “volume”, “SLA” | Verbos: “Registrar”, “Atribuir”, “Atualizar status” |

**Objetivo:** na **banca**, ser possível dizer em **10 segundos** qual tela é “macro” e qual é “minha fila”.

---

## Componentes principais (biblioteca)

- **Shell:** topbar (logo Central-LJ, ambiente, busca opcional), sidebar com itens: Missões, Nova missão, Dashboard (admin), Config (futuro).
- **Card de missão:** título, `pill` de prioridade, `pill` de status, tempo desde última atualização, tipo de ameaça (texto curto).
- **Tabela:** ordenação por prioridade/data; filtros inline ou barra de filtro colapsável.
- **Form “Nova missão”:** labels sempre visíveis, erros inline, botão primário único por tela.
- **Modal / drawer:** detalhe sem perder contexto da lista.
- **Toast:** sucesso (ACK), erro de rede, “fila indisponível”.
- **Empty state:** ilustração geométrica minimalista + CTA.

---

## Layout

- Grid **12 colunas**, margens laterais **24–32 px**, gutter **16–24 px**.
- **Desktop-first** na N1; tablet colapsa sidebar para ícones.

---

## Timeline / status da missão

- Linha **vertical** ou horizontal com marcos: *Recebida → Em análise → …* (alinhado ao enum do diagrama de classes).
- Cada marco: **hora** + **ator** (sistema / equipe) + texto curto opcional.
- Status: cor do token **e** rótulo textual (a11y).
- **N2:** eventos Kafka/WebSocket alimentam a timeline; no Figma hoje pode usar **dados fictícios fixos**.

---

## UX — check rápido

- Loading visível em envios; mensagem clara se Kafka ou API falhar.
- Explicar assíncrono quando fizer sentido: *“Registrado. Processamento em fila.”*
- Confirmação para ações destrutivas (cancelar missão crítica).
- Ícones: família única (ex. Lucide); tamanhos consistentes.

---

## Acessibilidade (meta)

- Contraste **AA** em textos principais sobre `surface`.
- Foco de teclado visível; ordem de tab lógica em formulários.
- Nunca comunicar estado **só** por cor.

---

## Relação com o código (N1)

O React atual pode usar cores próximas a `bg-deep` / `surface` e Inter via stack do sistema. O Figma é a **fonte da verdade** para espaçamentos, estados `hover/disabled` e variantes de componente nas próximas entregas.
