# Artefatos da N1 — Inventário

Este arquivo lista **tudo o que compõe a entrega N1**, onde encontrar no repositório, para que serve e **o que vale mostrar na apresentação**.

---

## Documentação narrativa e qualidade (`docs/n1/`)

| Artefato | Caminho | Conteúdo (resumo) | O que mostrar na banca |
|----------|---------|-------------------|------------------------|
| Visão geral | `01-visao-geral.md` | Problema, tema Central-LJ, objetivos, público, fluxo macro, por que não é CRUD genérico, papel do Kafka | Slide ou tópico 1–2 min: problema + proposta |
| Requisitos não funcionais | `02-requisitos-nao-funcionais.md` | RNFs por categoria (desempenho, segurança, observabilidade, etc.) ligados ao contexto | Tabela resumida ou “destaque 3 RNFs” |
| Plano de testes | `03-plano-de-testes.md` | Escopo N1, T1–T9, C1–C8, riscos, estratégia N2 | Um fluxo manual (ex.: C4 → C5 → C6) |
| Padrões de projeto | `04-padroes-de-projeto.md` | DI, DTO, Repository, Service, Strategy, Factory — onde e por quê na Central-LJ | 1 slide “padrões escolhidos” |
| Roteiro 5 min | `05-roteiro-apresentacao-n1.md` | Tempos, fala sugerida, dicas | Roteiro interno da equipe |
| Transição N2 | `transicao-para-n2.md` | Estado atual, pendências, prioridades, riscos, ordem de implementação | Fechamento: “próximos passos” |

---

## Arquitetura visual (`docs/arquitetura/`)

| Artefato | Caminho | Conteúdo | O que mostrar |
|----------|---------|----------|---------------|
| C4 Contexto | `c4-contexto.puml` | Usuário comum, operador/admin, frontend, backend, Kafka, PostgreSQL | Exportar imagem ou IDE PlantUML; apontar relações |
| C4 Contêiner | `c4-container.puml` | SPA React, Spring Boot, Kafka, Postgres, Kafka UI (dev) | Mesmo: enfatizar **contêineres** e portas (UI 8088) |
| Diagrama de classes | `diagrama-classes.puml` | Missão, status, evento, equipe, localização, etc. | Provar que o núcleo é **processo + evento** |

---

## Figma e identidade visual (`docs/figma/`)

| Artefato | Caminho | Conteúdo | O que mostrar |
|----------|---------|----------|---------------|
| Guia de estilos | `style-guide.md` | Paleta, tipografia, admin vs operador, timeline, UX | Referência ao abrir o arquivo Figma |
| Link e escopo | `link-figma.md` | URL (preencher), telas mínimas, checklist, “o que mostrar ao professor” | **Protótipo navegável** ao vivo |

---

## Consolidação da entrega (`docs/entrega/`)

| Artefato | Caminho | Conteúdo | O que mostrar |
|----------|---------|----------|---------------|
| Checklist N1 | `checklist-n1.md` | Itens marcáveis para entrega | Entrega formal / autoavaliação |
| Resumo executivo | `resumo-entrega-n1.md` | Uma página para revisor externo | Opcional: enviar por e-mail ao professor |
| Inventário (este arquivo) | `artefatos-n1.md` | Mapa de artefatos | Navegação rápida na pasta `docs/` |
| Ordem da apresentação | `ordem-apresentacao.md` | Sequência prática, tópicos, fala, divisão em grupo | Durante ensaio e no dia |

---

## Código executável (N1)

| Componente | Caminho | O que prova | Demo sugerida |
|------------|---------|-------------|---------------|
| Backend | `backend/` | API REST, produtor/consumidor Kafka mínimo | `mvn spring-boot:run`; logs ao consumir |
| Frontend | `frontend/` | Chamadas à API, UI de validação | `npm run dev`; health, missão teste, botão Kafka |
| Infra | `infra/` | Kafka, Kafka UI, Postgres no Compose | `up.ps1` ou `docker compose`; Kafka UI |

Leitura complementar: `backend/README.md`, `frontend/README.md`, `infra/kafka/README.md`.

---

## Materiais auxiliares

| Artefato | Caminho | Uso |
|----------|---------|-----|
| README raiz | `README.md` | Porta de entrada do repositório |
| Assets | `assets/` | Logo, imagens exportadas dos diagramas (se a equipe versionar) |

---

## Legenda rápida: N1 vs N2

| Incluído na N1 | Excluído propositalmente da N1 (N2+) |
|------------------|--------------------------------------|
| Docs, diagramas, Figma (guia + protótipo) | Fluxo completo de missão com regras reais |
| Endpoints de teste + Kafka testável | Persistência obrigatória no PostgreSQL ligada ao fluxo |
| Consumer mínimo (ex.: log) | Consumidor que atualiza banco e status |
| Hello world integrado | Autenticação completa, WebSocket em produção |

Coerência: qualquer texto que **prometa** N2 como já feito deve ser corrigido — a N1 **prepara** a N2.
