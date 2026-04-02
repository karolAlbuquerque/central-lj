# N2 — Módulo de heróis e equipes heroicas

## Visão de domínio

O sistema deixou de tratar apenas “ocorrências” abstratas e passou a ter **elenco operacional**: **heróis** e **equipes heroicas**, com vínculo opcional (um herói pode pertencer a uma equipe). Isso sustenta a narrativa da **Central de Missões da Liga da Justiça** sem explodir o modelo em dezenas de entidades.

## Entidades (backend)

| Entidade | Papel |
|----------|--------|
| **EquipeHeroica** | Agrupamento nomeado (`nome`, `especialidadePrincipal`, `ativa`, auditoria). |
| **Heroi** | Pessoa operacional: `nomeHeroico`, `nomeCivil` opcional, `especialidade`, `statusDisponibilidade` (`DISPONIVEL`, `EM_MISSAO`, `INATIVO`), `nivel`, `ativo`, associação **opcional** à equipe, timestamps. |
| **Mission** | Continua com ciclo de vida e histórico; ganhou campos opcionais de **atribuição**: `heroiResponsavel`, `equipeResponsavel`, `atribuidoEm`, `atribuidoPor`. |

Migração Flyway aplicável em Postgres: scripts em `backend/src/main/resources/db/migration/` (inclui tabelas de equipe e herói e colunas na missão).

## API REST principal

### Heróis (`HeroiController`, base `/api/heroes`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/heroes` | Cadastro |
| GET | `/api/heroes` | Lista |
| GET | `/api/heroes/{id}` | Detalhe + missões associadas (no payload agregado) |
| GET | `/api/heroes/{id}/missions` | Lista de **missões** em que o herói é responsável atual |
| PATCH | `/api/heroes/{id}/availability` | Atualiza `statusDisponibilidade` |

### Equipes (`TeamController`, base `/api/teams`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/teams` | Cadastro |
| GET | `/api/teams` | Lista |
| GET | `/api/teams/{id}` | Detalhe + **membros** (heróis com `equipeId` apontando para a equipe) |

Camadas: **DTOs**, **repositórios**, **serviços** (`HeroiService`, `EquipeHeroicaService`), injeção explícita nos controllers.

## Frontend — telas e rotas

Navegação global (layout): **Painel**, **Missões**, **Heróis**, **Equipes**, **Nova missão**.

| Rota | Conteúdo |
|------|----------|
| `/herois` | Lista: nome heroico, especialidade, badge de disponibilidade, equipe, link ao detalhe |
| `/herois/nova` | Formulário de cadastro |
| `/herois/:id` | Dados do herói, equipe, disponibilidade, lista de missões vinculadas |
| `/equipes` | Lista de equipes |
| `/equipes/nova` | Cadastro simples |
| `/equipes/:id` | Nome, especialidade, membros |
| `/missoes` | Lista dedicada de missões (complementa o painel) |

Componente auxiliar: `HeroAvailabilityBadge` para disponibilidade na tabela de heróis.

## Documentos relacionados

- Atribuição de missões: [08-atribuicao-de-missoes.md](08-atribuicao-de-missoes.md).
- Pendências pós-MVP: [03-pendencias.md](03-pendencias.md).
