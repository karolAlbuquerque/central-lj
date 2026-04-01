# Checklist de entrega — N1 (Central-LJ)

Use esta lista **antes da entrega** e **antes da apresentação**. Marque cada item quando estiver cumprido.

---

## Documentação N1 (`docs/n1/`)
- [ ] **Visão geral** (`01-visao-geral.md`) coerente com README e diagramas.
- [ ] **Requisitos não funcionais** (`02-requisitos-nao-funcionais.md`) completos por categoria.
- [ ] **Plano de testes** (`03-plano-de-testes.md`) com cenários C1–C8 reproduzíveis.
- [ ] **Padrões de projeto** (`04-padroes-de-projeto.md`) justificados no contexto Central-LJ.
- [ ] **Roteiro 5 min** (`05-roteiro-apresentacao-n1.md`) ensaiado.
- [ ] **Transição para N2** (`transicao-para-n2.md`) lida pela equipe.

## Consolidação da entrega (`docs/entrega/`)
- [ ] **Resumo da entrega** (`resumo-entrega-n1.md`) revisado (stack, decisões, próximos passos).
- [ ] **Índice de artefatos** (`artefatos-n1.md`) conferido (caminhos e o que mostrar na banca).
- [ ] **Ordem da apresentação** (`ordem-apresentacao.md`) combinada com a equipe.

## Arquitetura (`docs/arquitetura/`)
- [ ] **C4 Contexto** (`c4-contexto.puml`) renderizável; atores e sistemas corretos.
- [ ] **C4 Container** (`c4-container.puml`) alinhado à stack e ao que existe na N1.
- [ ] **Diagrama de classes** (`diagrama-classes.puml`) enxuto e coerente com o domínio.

## Figma e UX (`docs/figma/`)
- [ ] **Link final** colado em `link-figma.md` (URL pública ou de convidado).
- [ ] **Guia de estilos** (`style-guide.md`) refletido no protótipo (paleta, hierarquia).
- [ ] Checklist visual de `link-figma.md` verificado.

## Repositório e código
- [ ] Estrutura do monorepo clara (`frontend`, `backend`, `infra`, `docs`, `assets`).
- [ ] **Git** conforme exigência da disciplina (commits ou branches).
- [ ] **README raiz** como porta de entrada (links para docs importantes).

## Infraestrutura e demonstração
- [ ] **Docker Compose** sobe Kafka, Kafka UI e Postgres sem passos não documentados.
- [ ] Scripts `infra/scripts/up.ps1`, `down.ps1`, `reset.ps1` testados no ambiente da equipe.
- [ ] **Hello world** front ↔ back (health + hello na UI ou via HTTP).
- [ ] **`POST /api/missions/test`** responde sem depender do Kafka.
- [ ] **`POST /api/events/publish-test`** publica no tópico configurado.
- [ ] **Consumer** deixa evidência nos logs do backend.
- [ ] **Kafka UI** (`http://localhost:8088`) mostra mensagens no fluxo de demo.

## Qualidade técnica mínima
- [ ] `mvn test` (backend) passa.
- [ ] `npm run build` (frontend) passa.
- [ ] Sem contradição grave entre documentação e comportamento real da API.

---

## Critério: N1 fechada para entrega

Marque esta etapa quando **todos** os blocos acima estiverem aderentes. A N1 está **madura** se um avaliador consegue:

1. Ler o **README** e subir infra + backend + frontend.
2. Reproduzir **C1–C8** do plano de testes.
3. Navegar **docs/n1**, **`docs/entrega`**, diagramas e Figma sem lacunas graves.

**Pendências aceitáveis (fora do repositório):** apenas itens operacionais da equipe (ex.: link Figma definitivo, se ainda não publicado).

---

## Próximo marco

Após fechamento desta checklist, o projeto segue para a **N2** (ver `docs/n1/transicao-para-n2.md`): persistência PostgreSQL no fluxo de negócio, evolução de status via consumidores, endurecimento de segurança e testes de integração mais amplos — **sem misturar escopo N2 na entrega N1**.
