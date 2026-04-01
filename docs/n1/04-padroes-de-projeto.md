# N1 — Padrões de projeto e justificativas

Este documento explica **quais** padrões adotamos (ou adotaremos) na Central-LJ e **por que** fazem sentido para uma **central de missões** orientada a eventos — não é uma lista genérica, e sim um mapeamento para o nosso domínio (missão, equipe, ameaça, status, eventos, Kafka).

---

## Injeção de dependência (Spring IoC)
**O que é:** o framework injeta implementações (serviços, produtores Kafka, repositórios) nos construtores em vez de classes criarem dependências diretamente.

**Onde se aplica:** `MissionTestService`, `EventPublishTestService`, `MissionEventProducer`, controllers, futuros `MissionService` e repositórios.

**Por que na Central-LJ:** missões vão acumular regras (priorização, alocação de equipe). Sem DI, o código vira um emaranhado de `new` e fica impossível **testar cenários** (ex.: simular fila Kafka indisponível). O container Spring é padrão de mercado e defensável na banca.

---

## DTO (Data Transfer Object)
**O que é:** objetos imutáveis ou simples que representam **entrada e saída da API** (JSON), separados das entidades de persistência e do modelo rico de domínio.

**Onde se aplica:** `MissionTestRequest`, `EventPublishTestRequest`, respostas JSON dos controllers; futuros DTOs de leitura de missão para o React.

**Por que na Central-LJ:** o contrato com o **React** deve ser **estável** e explícito. O operador não precisa ver detalhes internos de ORM; eventos expostos diretamente podem vazar campos internos. DTOs também versionam melhor quando o domínio evoluir (N2).

---

## Camada de serviço (Service Layer)
**O que é:** classes `@Service` concentram **casos de uso** (“aceitar missão de teste”, “publicar evento de infraestrutura”), orquestrando repositórios e mensageria.

**Onde se aplica:** `MissionTestService`, `EventPublishTestService`; futuro `MissionLifecycleService` coordenando persistência + Kafka.

**Por que na Central-LJ:** uma missão não é só “salvar”: envolve validações, efeitos colaterais (evento) e regras. A camada de serviço evita controllers inchados e espelha a **linguagem do domínio** (“aceitar”, “publicar”, “priorizar”), facilitando leitura por professores e pares.

---

## Repository
**O que é:** abstração de acesso a dados (Spring Data JPA ou similar), expondo operações como `findById`, `save`, consultas por status.

**Onde se aplica:** Pacote `repository/` (preparado na N1); implementação concreta na N2 com PostgreSQL.

**Por que na Central-LJ:** o modelo de **Missao**, **Usuario**, **EventoMissao** precisa persistir de forma **trocaável** (testes com H2, produção com Postgres). Repository isola SQL/JPQL e permite testar serviços com **dublês**.

---

## Strategy (estratégia)
**O que é:** família de algoritmos intercambiáveis atrás de uma interface comum (ex.: `PriorizacaoStrategy`).

**Onde se aplicará:** priorização por **tipo de ameaça**, severidade ou SLAs diferentes; roteamento para **equipes heroicas** distintas.

**Por que na Central-LJ:** a central não pode ficar presa a um único “if” de prioridade. Novos tipos de incidente (nomeadamente no tema “liga”) exigem **estratégias plugáveis**, testáveis individualmente — aderente ao princípio aberto/fechado.

---

## Factory (fábrica)
**O que é:** componente responsável por **criar** objetos complexos ou eventos versionados com campos obrigatórios sempre preenchidos.

**Onde se aplicará:** criação padronizada de `EventoMissao` / payloads Kafka (tipo, ids, timestamp, versão de schema).

**Por que na Central-LJ:** eventos mal formados no Kafka geram **corrupção silenciosa** downstream. Uma fábrica centraliza invariantes (“todo evento tem `missionId` e `eventId`”) e reduz duplicação entre produtores.

---

## Mensageria orientada a eventos (produtor / consumidor)
**O que é:** produtores publicam mensagens em tópicos; consumidores leem em grupos e processam de forma assíncrona.

**Onde se aplica:** pacotes `messaging.producer` e `messaging.consumer`; configuração Spring Kafka.

**Por que na Central-LJ:** é o núcleo da **não-CRUDidade** do projeto — o sistema reage a **fatos** (`MissaoRecebida`, `StatusAlterado`) em vez de apenas atualizar uma linha sem narrativa. Na N1 isso aparece de forma mínima; na N2, vira pipeline real.

---

## Configuration Properties (complementar)
Agrupamos configurações (`central-lj.*`) com `@ConfigurationProperties` para CORS, tópicos e flags (ex.: ligar/desligar consumer em testes). Isso mantém o código livre de **strings mágicas** e alinha com as boas práticas do Spring Boot 3.x.

---

## Resumo para defesa oral
> “Usamos **DTO** e **Service** para manter a API do operador clara; **Repository** para isolar o Postgres que virá; **Strategy** e **Factory** para não petrificar regras de missão e eventos; **Kafka** como contrato assíncrono entre aceitar ocorrência e processá-la.”
