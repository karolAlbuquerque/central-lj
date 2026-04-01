# Infraestrutura local — Kafka e serviços de apoio

Este diretório documenta o **ambiente de desenvolvimento** da Central-LJ. O arquivo de orquestração principal está em `infra/docker-compose.yml`.

Para **checklist de entrega** e ordem de apresentação, ver `docs/entrega/` na raiz do monorepo.

## O que sobe
| Serviço | Porta local | Função |
|--------|-------------|--------|
| **Kafka** | `9092` | Broker; tópicos `missions.created` (N2) e `missions.events` (teste N1). |
| **Kafka UI** | `8088` | Inspeção de tópicos e mensagens. |
| **PostgreSQL** | **`5433` →** container `5432` | **Obrigatório para o backend N2** (Flyway + JPA). No PC use **`localhost:5433`**. Credenciais: banco `central_lj`, usuário `central_lj`, senha `central_lj`. |

## Pré-requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (ou equivalente com Compose v2)
- PowerShell com permissão para executar scripts **ou** uso direto de `docker compose` (ver abaixo)

### Execução de scripts no Windows
Se `up.ps1` for bloqueado:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

## Subir o ambiente
Na raiz do monorepo (`central-lj/`):

```powershell
.\infra\scripts\up.ps1
```

Equivalente sem script:

```powershell
docker compose -f infra\docker-compose.yml up -d
```

## Verificar se está no ar
1. `docker compose -f infra/docker-compose.yml ps` — containers **Running**
2. Navegador: `http://localhost:8088` — Kafka UI com cluster `central-lj-local`
3. (Opcional) `docker logs central-lj-kafka` — sem erros fatais repetidos

## Derrubar / resetar
```powershell
.\infra\scripts\down.ps1
.\infra\scripts\reset.ps1
```

`reset.ps1` remove também os **volumes** (apaga dados locais do Postgres e estado auxiliar).

## Testar fluxo N2 (recomendado)
1. Garanta **Postgres** e **Kafka** no ar (`up.ps1`).
2. Suba o **backend** e o **frontend**.
3. Crie uma missão pela UI ou `POST http://localhost:8080/api/missions` (JSON com `titulo`, `tipoAmeaca`, `prioridade`, etc.).
4. Na **Kafka UI**, inspecione o tópico **`missions.created`**.
5. Acompanhe os logs do backend (`[Central-LJ][Workflow]`) e a lista no frontend (polling).

## Testar publicação legada N1 (`missions.events`)
1. `POST http://localhost:8080/api/events/publish-test` com corpo opcional `{ "mensagem": "teste manual" }`
2. Consumer do tópico `missions.events` registra no log `[Central-LJ][Kafka] Evento recebido: ...`

> `POST /api/missions/test` é só ACK HTTP legado; o fluxo real de domínio é **`POST /api/missions`**.

## Variáveis úteis
- `KAFKA_BOOTSTRAP_SERVERS` — padrão `localhost:9092`
- `CENTRAL_LJ_KAFKA_TOPIC` — padrão `missions.events` (teste)
- `CENTRAL_LJ_KAFKA_TOPIC_CREATED` — padrão `missions.created` (domínio N2)
- `DATABASE_URL` — padrão `jdbc:postgresql://localhost:5433/central_lj` (porta **host** do Compose)
- `DATABASE_USER` / `DATABASE_PASSWORD` — como no Compose (`central_lj` / `central_lj`)

Se aparecer **erro de autenticação (28P01)** ao subir o Spring, costuma ser: Postgres **local** na 5432 respondendo no lugar do container → use **`5433`** ou `.\infra\scripts\reset.ps1` para recriar o volume.
