# N1 — Visão geral do projeto

## Contexto do problema
Organizar ocorrências críticas — “missões” — exige mais do que um cadastro estático: é preciso **priorizar**, **atribuir responsáveis**, **evoluir o status ao longo do tempo** e manter **rastreabilidade** do que aconteceu e quando. Quando muitas missões chegam ao mesmo tempo, uma arquitetura unicamente síncrona tende a saturar a API e atrasar respostas ao usuário.

## Justificativa do tema “Central de Missões da Liga da Justiça”
O tema **Central de Missões da Liga da Justiça (Central-LJ)** traduz esse problema para um contexto memorável e acadêmico: uma **central web de operações heroicas** onde cada ocorrência é uma missão com ciclo de vida (recebida → análise → priorização → equipe → execução → conclusão ou cancelamento). O nome facilita a comunicação na banca, sem reduzir o problema técnico: trata-se de um **sistema web distribuído**, com **frontend**, **backend**, **mensageria** e **banco relacional previsto**, alinhado a soluções reais de centrais de comando e sistemas orientados a eventos.

## Objetivo geral
Construir a **Central-LJ** como plataforma web para registrar e acompanhar missões, sustentada por uma arquitetura que permita **escala**, **processamento assíncrono** e **evolução futura de regras** — sem tratar o domínio como um CRUD genérico de “registros”.

## Objetivos específicos (N1)
- Entregar **monorepo** organizado (`frontend`, `backend`, `infra`, `docs`, `assets`).
- Disponibilizar **ambiente local** com **Kafka** (e UI de apoio) via Docker Compose.
- Provar **integração** frontend ↔ backend e **publicação/consumo** mínimo de eventos no Kafka.
- Documentar **visão**, **RNFs**, **testes**, **padrões**, **arquitetura (C4 + domínio)** e **guia visual (Figma)**.
- **Não** fechar o fluxo completo de negócio nem a persistência total — isso é objeto das próximas etapas (N2+).

## Público-alvo
| Papel | Necessidade |
|--------|-------------|
| **Usuário comum** | Em evolução: consulta ou interação simplificada com a central (fora do escopo completo da N1). |
| **Operador** | Registrar missões, acompanhar status e receber feedback claro da central. |
| **Administrador / coordenação** | Visão agregada, priorização e indicadores (dashboard planejado pós-N1). |

## Fluxo macro da missão (produto alvo)
1. **Entrada:** uma missão é registrada (título, contexto, tipo de ameaça, local, prioridade inicial quando aplicável).
2. **API:** valida e persiste (em N2+) e/ou emite **eventos** que representam fatos do domínio.
3. **Kafka:** transporta eventos de forma assíncrona e durável entre produtores e consumidores.
4. **Processamento:** consumidores aplicam regras (priorização, designação de equipe, transições de estado).
5. **Consulta:** a interface apresenta status atual e, idealmente, **linha do tempo** de eventos; atualização em tempo real via **WebSocket** (planejado).

## Por que este projeto não é um CRUD simples
Um CRUD genérico expõe apenas **Create/Read/Update/Delete** sobre entidades estáticas. A Central-LJ trata **missões** como **processos** com **estado**, **eventos** e **orquestração assíncrona**: o que importa não é só “salvar uma linha”, mas **capturar o que aconteceu** (evento), **quem reagiu** (consumidor) e **como o estado evolui** — padrão típico de sistemas distribuídos modernos, não de formulário isolado.

## Resumo da arquitetura proposta
- **React (Vite):** camada de apresentação e experiência do operador.
- **Spring Boot:** API REST, validação, futura persistência em **PostgreSQL**, integração **Kafka** (produtor/consumidor), base para **WebSocket** futuro.
- **Kafka:** desacoplamento entre “aceitar o pedido” e “processar a missão”; permite múltiplos consumidores e replay/auditoria quando bem modelado.
- **PostgreSQL:** em N2+, armazena o estado autoritativo das missões e histórico.
- **Docker Compose:** reprodutibilidade do laboratório acadêmico (Kafka, UI, Postgres).

## Papel do Kafka no fluxo
O Kafka é o **backbone de eventos**: quando a API (ou um consumidor downstream) **publica** um evento (ex.: missão recebida, status alterado), outros componentes **assistem** a esse fluxo sem precisar ser chamados de forma síncrona. Na **N1**, isso é demonstrado com um **endpoint de publicação de teste** e um **consumidor mínimo** (ex.: log); na **N2**, os mesmos tópicos e contratos evoluem para **regras reais** e **atualização de persistência**.
