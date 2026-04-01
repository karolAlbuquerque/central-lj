# N1 — Roteiro de apresentação (~5 minutos)

**Materiais complementares:** sequência por tela e divisão em grupo em [`docs/entrega/ordem-apresentacao.md`](../entrega/ordem-apresentacao.md); fechamento institucional da fase em [`transicao-para-n2.md`](transicao-para-n2.md).

---

## Estrutura sugerida (com tempos)

### 1. Abertura (~30 s)
- Apresente o nome **Central de Missões da Liga da Justiça (Central-LJ)**.
- Afirme o foco da **N1**: base **executável**, **documentada** e **orientada a eventos** — não o sistema final.

### 2. Problema (~45 s)
- Muitas ocorrências simultâneas: resposta rápida + rastreabilidade.
- Processamento só síncrono: gargalo; necessidade de **desacoplar** “receber missão” de “processar missão”.

### 3. Solução proposta (~45 s)
- **Central web** para registrar e acompanhar missões.
- **Arquitetura distribuída**: React + Spring Boot + Kafka + PostgreSQL (previsto).
- **Docker Compose** para laboratório reprodutível.

### 4. Stack (~30 s)
- **React (Vite + TypeScript)** — interface operacional.
- **Spring Boot** — API REST, integração Kafka, base para persistência e WebSocket futuro.
- **Apache Kafka** — eventos de domínio.
- **PostgreSQL** — persistência na N2+.

### 5. Arquitetura (~60 s)
- Mostrar **C4 Contexto**: usuários, frontend, backend, Kafka, PostgreSQL.
- Mostrar **C4 Contêiner**: SPA, backend, Kafka, Postgres, Kafka UI (dev).
- **Diagrama de classes**: Missao, Status, EventoMissao, equipes — processo, não CRUD solto.

### 6. Papel do Kafka (~45 s)
- Backbone de **eventos**: fatos imutáveis circulam na fila; consumidores evoluem estado sem bloquear o HTTP.
- **N1:** `POST /api/events/publish-test` + consumer mínimo (log); **N2:** regras reais e banco.

### 7. Figma e UX (~30 s)
- **Style guide** futurista, tecnológico e profissional; Figma como referência de telas e fluxo.
- N1 no código: demonstração técnica alinhada à identidade visual acordada.

### 8. Qualidade e testes (~30 s)
- **RNFs** documentados (desempenho, escala, segurança planejada, observabilidade).
- **Plano de testes** com cenários C1–C8; builds `mvn test` e `npm run build`.
- **Padrões**: DI, DTO, Service, Repository, Strategy/Factory planejados.

### 9. Fechamento (~30 s)
- Recapitular o entregável N1: **repositório + infra + hello world distribuído**.
- **Próximo passo (N2):** PostgreSQL, fluxo completo de missão, consumidores com regras, autenticação, WebSocket.

---

## Fala curta (quase literal — ~5 min)

*Bom dia / boa tarde. Somos o projeto **Central de Missões da Liga da Justiça**, uma central web inspirada em operações coordenadas de alto impacto.*

*O problema que endereçamos é clássico em sistemas críticos: chegam muitas ocorrências ao mesmo tempo e precisamos registrar, priorizar e acompanhar cada “missão” com rastreabilidade. Se tudo for síncrono num único monólito, a API vira gargalo.*

*Nossa proposta é distribuir responsabilidades: **React** na interface do operador, **Spring Boot** na API, e **Kafka** como espinha dorsal de **eventos** que representam o que aconteceu na missão — recebida, em análise, equipe designada, concluída. O **PostgreSQL** será a fonte da verdade na próxima etapa.*

*Na **N1** não entregamos o produto inteiro: entregamos uma **base defensável**: Docker com Kafka, integração front-back, um endpoint que confirma missão de teste e outro que **publica evento** no Kafka, com um consumidor mínimo que prova o encadeamento. Documentamos **requisitos não funcionais**, **plano de testes**, **padrões de projeto** — DI, DTO, camada de serviço, repositório, Strategy e Factory para quando as regras de priorização crescerem.*

*Temos **diagramas C4** e de **domínio** para explicar por que isso **não é um CRUD genérico**: o coração é o **fluxo** e os **eventos**, não apenas quatro operações sobre uma tabela.*

*O **Figma** e o **style guide** alinham a identidade visual — futurista, operacional, heroica — com o que implementamos hoje como demonstração.*

*Em resumo: na N1 mostramos **arquitetura**, **mensageria** e **qualidade de documentação**. Na **N2** amarramos persistência, regras completas, segurança e atualização em tempo real. Obrigado—abraço para perguntas.*

---

## Dicas para a banca
- Tenha os diagramas exportados ou abertos no IDE; aponte **com o cursor** cada ator e contêiner.
- Se demonstrar ao vivo: **infra** → **backend** → **frontend** → **Kafka UI** → **log do consumer**.
- Se alguém perguntar “onde está o CRUD”: responda com **evento + estado + consumidor**, não só “tabela + formulário”.
