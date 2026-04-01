# Resumo executivo — Entrega N1 (Central-LJ)

Documento de **uma página** para professores, coordenadores ou revisores externos que precisam entender o marco N1 em poucos minutos.

---

## O que é o projeto

**Central de Missões da Liga da Justiça (Central-LJ)** é um trabalho acadêmico de **sistema web distribuído** para registrar e acompanhar **missões operacionais** (com ciclo de vida, prioridade e eventos), inspirado em uma central de comando. O foco da N1 é **arquitetura demonstrável** e **documentação defensável**, não o produto final completo.

---

## Objetivo da fase N1

- Entregar **monorepo** organizado com **frontend**, **backend**, **infra Docker** e **documentação**.
- Provar **integração React ↔ Spring Boot** e **publicação/consumo mínimo** via **Apache Kafka**.
- Documentar **visão**, **requisitos não funcionais**, **plano de testes**, **padrões de projeto**, **diagramas C4 e de domínio**, **guia Figma** e **roteiros de apresentação**.
- **Delimitar** claramente o que **não** é N1 (persistência completa, autenticação, fluxo de negócio integral) — ver `transicao-para-n2.md`.

---

## Stack utilizada

| Camada | Tecnologia |
|--------|------------|
| Interface | React 19 + Vite + TypeScript |
| API | Spring Boot 3.x, Java 17 |
| Mensageria | Apache Kafka |
| Dados (laboratório) | PostgreSQL 16 no Compose (uso obrigatório no fluxo de aplicação apenas na N2) |
| Orquestração local | Docker Compose + scripts PowerShell |

---

## Principais decisões arquiteturais

1. **Orientação a eventos:** o domínio prevê **fatos (eventos)** circulando no Kafka, não apenas persistência síncrona ponta a ponta.
2. **Separação explícita na N1:** `POST /api/missions/test` **confirma** recebimento **sem** Kafka; `POST /api/events/publish-test` **exercita** a mensageria — evita confundir “formulário” com “fila”.
3. **Desacoplamento para N2:** produtores e consumidores evoluem para regras de negócio e atualização de estado persistido, mantendo o mesmo eixo tecnológico.
4. **Diagramas como contrato acadêmico:** C4 Contexto e Contêiner amarram decisões à stack; diagrama de classes fixa o **núcleo do domínio** (missão, status, evento, equipe).

---

## Papel do Apache Kafka

Kafka é o **backbone assíncrono**: transporta eventos de domínio (e, na N1, um **evento de teste**) entre o backend e consumidores. Isso permite escalar leitores, desacoplar latência HTTP de processamento e preparar auditoria/replay. Na N1, o consumidor é **mínimo** (ex.: log); na N2, espera-se **efeito** em estado persistido e regras.

---

## O que será implementado na N2 (visão)

- Modelagem e **persistência** real em **PostgreSQL** (missões, histórico de status, usuários quando aplicável).
- **Fluxo completo** de missão com eventos de negócio (não só endpoints de prova).
- **Autenticação/autorização** e papéis (operador, admin), conforme escopo disciplinar.
- **Consumidores** com lógica de negócio; evolução opcional para **WebSocket/SSE** e UI alinhada ao Figma completo.

Detalhamento: `docs/n1/transicao-para-n2.md` e `docs/n1/03-plano-de-testes.md` (estratégia N2).

---

## Onde começar no repositório

1. **README.md** na raiz — execução e links.
2. **`docs/entrega/artefatos-n1.md`** — mapa de todos os artefatos.
3. **`docs/n1/01-visao-geral.md`** — narrativa do problema e da solução.
