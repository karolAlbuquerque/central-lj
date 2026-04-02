# Resumo final — entrega N2 (Central-LJ)

## Visão geral

Plataforma **Central de Missões da Liga da Justiça**: operador registra missões, acompanha **status** e **linha do tempo** persistida, com processamento **assíncrono via Kafka** e atualização **quase em tempo real (SSE + polling)** no painel React.

## Stack

- React (Vite, TypeScript), Spring Boot 3.4 / Java 17, PostgreSQL, Apache Kafka, Docker Compose.

## Fluxo implementado

1. `POST /api/missions` valida e persiste (**RECEBIDA** + histórico).  
2. Após **commit**, publica **`MISSION_CREATED`** em **`missions.created`** e notifica clientes SSE.  
3. **Consumer** ingere o evento, executa **workflow** (Strategy), grava histórico por transição, notifica SSE.  
4. Frontend exibe **dashboard**, **filtros por status**, **detalhe** com timeline.

## Diferenciais técnicos

- **Orientação a eventos** sem abandonar REST para o caso de uso principal.  
- **Histórico audível** (`mission_history` + origem API vs Kafka).  
- **Separação messaging**: ingestão, producer, dispatch pós-commit, consumer mínimo.  
- **Strategy** para prioridade crítica vs fluxo padrão.

## Mensageria

- **`missions.created`:** contrato estável (`MissionCreatedKafkaEvent`), consumido por `MissionCreatedConsumer` → `MissionCreatedEventIngestionService` → `MissionWorkflowService`.  
- **`missions.events`:** apenas **observabilidade** e teste manual (`/api/events/publish-test`).

## Qualidade de código

- DTOs, repositories, services, injeção de dependência; controllers finos.  
- Tratamento centralizado de erro (`ApiExceptionHandler`).

## Testes

- Integração API (MockMvc), workflow sem Kafka, **testes unitários** de ingestão, resolver de strategy, dispatch pós-commit.  
- **JaCoCo:** relatório em `backend/target/site/jacoco/index.html` após `.\mvnw.cmd test` (Windows).

## Fidelidade ao protótipo

- Paleta e hierarquia alinhadas ao **`docs/figma/style-guide.md`**; tipos **Space Grotesk**, **Inter**, **IBM Plex Mono**; painel com cards, tabela e filtros; timeline com destaque por origem (API / Kafka / erro). Não há paridade pixel-perfect com Figma (documentado como pendência).
