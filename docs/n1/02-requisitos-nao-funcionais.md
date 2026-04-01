# N1 — Requisitos não funcionais (RNFs)

Os RNFs abaixo orientam decisões de arquitetura da **Central-LJ** (React + Spring Boot + Kafka + PostgreSQL previsto). São desejáveis no **produto final**; na **N1** parte deles é **endereçada de forma mínima** (ex.: health check, logs do consumer), e o texto deixa explícito o que será reforçado na N2.

---

## Desempenho
**Descrição:** A interface deve permanecer responsiva; a API deve responder rapidamente às operações síncronas que o usuário percebe (ex.: confirmação de envio de missão de teste).

**Justificativa:** Picos de ocorrências não podem travar a experiência do operador. O processamento pesado deve migrar para **consumidores Kafka**, mantendo a rota HTTP **enxuta**.

**N1:** Endpoints `GET /api/health` e `GET /api/hello` com baixa latência local; `POST /api/missions/test` devolve ACK sem bloquear em lógica pesada.

---

## Escalabilidade
**Descrição:** O backend deve ser **stateless** onde possível; consumidores Kafka devem poder escalar horizontalmente (mais instâncias, mesmo `group.id`).

**Justificativa:** Diferentes equipes ou filas de processamento poderão absorver mais missões aumentando **réplicas** de consumidor, não apenas CPU de um monólito.

**N1:** Demonstração conceitual via Docker e código preparado em pacotes (`messaging`); escala real em produção é objeto de etapas futuras.

---

## Disponibilidade
**Descrição:** Em ambiente de produção (alvo), falha parcial de um consumidor não deve derrubar a API de entrada; filas devem permitir retomada.

**Justificativa:** Central de missões precisa continuar **recebendo** ocorrências mesmo quando um pipeline de processamento está indisponível temporariamente.

**N1:** Apache Kafka local com volumes; tratamento amigável quando o broker está fora (ex.: erro controlado na publicação de teste).

---

## Confiabilidade
**Descrição:** Eventos publicados devem ser **consistentes** com o contrato acordado (tipo, identificadores, timestamp); consumidores idempotentes quando possível (N2+).

**Justificativa:** Perda silenciosa de missões ou duplicação sem controle compromete a confiança na central.

**N1:** Contrato mínimo de evento de teste; logs do consumer para auditoria de laboratório.

---

## Segurança
**Descrição:** Dados de missões e identidades de operadores exigem autenticação, autorização por papel e proteção de transporte (HTTPS em produção).

**Justificativa:** Informação operacional sensível não pode ser exposta como API pública anônima.

**N1:** Sem autenticação completa; uso de variáveis de ambiente e `.gitignore` para segredos; CORS configurado para desenvolvimento.

---

## Usabilidade
**Descrição:** Linguagem e layout orientados ao operador; feedback imediato (sucesso, erro, “processando”) e redução de ambiguidade em status.

**Justificativa:** Ambiente de crise operacional exige clareza; o operador não pode adivinhar se a missão foi aceita.

**N1:** Frontend de demonstração com health, hello, formulário de missão de teste e publicação Kafka com resposta visível.

---

## Acessibilidade
**Descrição:** Contraste adequado, foco visível, navegação por teclado onde aplicável (WCAG 2.x como referência).

**Justificativa:** Profissionais diversos utilizam a central; exclusão por UI frágil é inaceitável em produto institucional.

**N1:** Guia de estilos (`docs/figma/style-guide.md`) define contraste base; implementação completa de A11y no React é gradual (N2+).

---

## Manutenibilidade
**Descrição:** Código em camadas (`controller`, `dto`, `service`, `repository`, `messaging`), nomenclatura estável, documentação em `docs/` alinhada ao repositório.

**Justificativa:** A Central-LJ evoluirá com novos status, regras e integrações; estrutura clara reduz custo de mudança.

**N1:** Monorepo e pacotes refletem a arquitetura pretendida; RNFs e diagramas guiam implementação futura.

---

## Observabilidade
**Descrição:** Logs correlacionáveis (ex.: `missionId`, `eventId`), métricas de fila e tracing distribuído em produção.

**Justificativa:** Sem visibilidade, falhas em consumidores Kafka viram “sumiço” de missões aparente.

**N1:** `GET /api/health` no contrato da API; opcionalmente `GET /actuator/health` se o Actuator estiver exposto (Spring Boot); logs do consumidor de teste ao receber mensagem no Kafka.

---

## Compatibilidade
**Descrição:** Frontend e backend desacoplados via **contrato REST/JSON**; evolução de versão de API documentada; stack fixada (React, Spring Boot 3.x, Kafka compatível com cliente Spring).

**Justificativa:** Permite trocar detalhes de implementação sem reescrita total da interface.

**N1:** Proxy Vite + CORS com padrões de origem para dev; variáveis `KAFKA_BOOTSTRAP_SERVERS` e tópico configurável.

---

## Síntese N1 vs. alvo
| RNF | N1 | Alvo (N2+) |
|-----|----|------------|
| Desempenho percebido | Endpoints leves | Fila + SLA por etapa |
| Escalabilidade | Base de código | Réplicas + particionamento |
| Segurança | Mínima (dev) | AuthN/AuthZ + HTTPS |
| Observabilidade | Log + health | Métricas + tracing |
| Acessibilidade | Guia + baseline UI | Auditoria WCAG nas telas |
