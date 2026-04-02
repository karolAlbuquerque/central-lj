# Roteiro — demonstração final N2 (7–10 minutos)

Objetivo: mostrar **proposta**, **arquitetura**, **fluxo com Kafka**, **UI** e **testes** sem se perder no meio.

---

## 0: Abertura (45 s)

- **Problema:** central precisa registrar missões e acompanhar evolução com rastreabilidade.  
- **Proposta:** API + PostgreSQL + **Kafka** para processar ciclo de vida sem travar o HTTP; **SSE** para o painel.

## 1: Arquitetura (1 min)

- Desenho mental: **React** → **Spring Boot** → **Postgres**; **Spring** → **Kafka** → **Consumer** → **Postgres**; **SSE** → React.  
- Frase-chave: *“commit primeiro, evento depois, processamento assíncrono”*.

## 2: Infra (30 s — opcional se já estiver no ar)

- Mencionar Compose: Postgres **5433**, Kafka **9092**, Kafka UI **8088**.

## 3: Criação de missão (1,5 min)

- Abrir **Nova missão**, preencher título, tipo, prioridade (se quiser, **CRÍTICA** para variar fluxo).  
- Enviar → mostrar **detalhe** com status **RECEBIDA** e primeira linha no histórico (**API_REGISTRO**).

## 4: Kafka no fluxo (1,5 min)

- Kafka UI: tópico **`missions.created`** — mostrar payload **MISSION_CREATED**.  
- Voltar ao detalhe: após poucos segundos, timeline aumenta com passos **KAFKA_WORKFLOW**.

## 5: Atualização de status (45 s)

- Apontar transições na timeline; **badge** de status no topo.

## 6: Dashboard (1 min)

- Painel: **cards** (total, andamento, concluídas, falhas).  
- **Tabela** com **filtros** (ex.: Concluídas / Falhas).  
- Mencionar **SSE + polling** como redundância.

## 7: Testes (45 s)

- Terminal: `.\mvnw.cmd test` (ou `mvn test` se Maven global; ou mostrar JaCoCo já gerado).  
- Uma frase: *“integração API + workflow + unitários da ingestão Kafka”*.

## 8: Encerramento (30 s)

- **Decisões:** histórico persistido, strategy para crítica, mensageria desacoplada.  
- **Honestidade:** auth, Testcontainers, Figma 100% — **fora do escopo N2**.

---

## Tempos — ajuste fino

| Bloco | Alvo |
|--------|------|
| Abertura + arquitetura | ~2 min |
| Demo criar + Kafka + timeline | ~3 min |
| Dashboard | ~1 min |
| Testes + fechamento | ~1,5 min |

**Se atrasar:** cortar Kafka UI e mostrar só timeline + painel.
