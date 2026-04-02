# N2 — Autenticação e papéis (MVP)

## Visão

A Central-LJ passou a ter **login real**, **JWT stateless** e **papéis** (`ADMIN`, `HERO`, reserva `OPERATOR`) para separar o **painel administrativo** da **área do herói**. O vínculo explícito entre **conta de usuário** e **entidade Heroi** garante coerência de produto: um herói logado enxerga apenas o que faz sentido no domínio.

## Modelagem

| Elemento | Descrição |
|----------|-----------|
| **Usuario** | `nome`, `email` (único), `senha_hash` (BCrypt), `role`, `ativo`, `heroi` opcional (`ManyToOne`), auditoria. |
| **Regras de integridade** | Papel `HERO` exige `heroi_id` não nulo; `ADMIN` não possui `heroi_id`. |
| **Flyway** | `V4__auth_usuarios.sql` cria a tabela `usuarios`. |

## Segurança (backend)

- **Spring Security** + filtro **JWT** (`Authorization: Bearer <token>`).
- **Endpoints públicos** (sem token): `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/health`, `GET /api/hello`, `POST /api/missions/test`, `POST /api/events/**`, `actuator` (conforme configuração), pré-flight **OPTIONS**.
- **Proteção por papel** (`@PreAuthorize`): listagens e comandos administrativos de missões, heróis (cadastro/listagem), equipes e atribuição exigem `ADMIN` ou `OPERATOR`. Detalhe de missão e histórico exigem autenticação; herói só acessa missão **designada a ele** (`MissionViewPolicy`).
- **JWT**: claims `sub` (id do usuário), `email`, `nome`, `role`, `heroiId` opcional. Segredo e validade em `central-lj.security.jwt` (`application.yml`). Em produção, use **`JWT_SECRET`** com string longa (≥ 32 caracteres para HS256).

## API de autenticação

| Método | Caminho | Descrição |
|--------|---------|------------|
| POST | `/api/auth/login` | Corpo: `{ "email", "password" }`. Resposta: `accessToken`, `tokenType`, `user` (`id`, `nome`, `email`, `role`, `heroiId`). |
| GET | `/api/auth/me` | Retorna o usuário atual a partir do JWT (401 sem sessão válida quando segurança ativa). |
| POST | `/api/auth/logout` | No MVP stateless: resposta **204**; o cliente descarta o token. |

## Missões do usuário logado

| Método | Caminho | Comportamento |
|--------|---------|----------------|
| GET | `/api/me/missions` | Para **HERO**: missões em que o `heroiId` do JWT é o responsável atual. Para **ADMIN/OPERATOR**: lista vazia (use o painel). |

## Demonstração (seed controlado)

Na **primeira subida** com banco vazio, se `central-lj.auth.demo-seed=true` (padrão em `dev`), o bean `DemoAuthDataLoader` cria:

- Equipe e herói **Guardião Demo** (IDs fixos documentados no código em `DemoAuthDataLoader`).
- Usuário **ADMIN**: e-mail `coordenacao@central-lj.demo`, senha `Admin@demo2026`.
- Usuário **HERO**: e-mail `heroi.demo@central-lj.demo`, senha `Hero@demo2026`, vinculado ao herói demo.

Para ambientes automatizados de teste (`application-test`), `demo-seed` fica **desligado**.

**Importante:** credenciais são para **demo acadêmica / banca** — não reutilize em produção; desligue o seed (`demo-seed: false`) e gerencie usuários por processo próprio quando for o caso.

## Frontend

- Rota **`/login`** com `AuthProvider` + persistência do token em `localStorage` (`central_lj_token`).
- **ADMIN/OPERATOR** (hoje o operador usa o mesmo shell do admin): navegação completa (Painel, Missões, Heróis, Equipes, Nova missão).
- **HERO**: layout dedicado (**Minha área**, **Minhas missões**, **Perfil** opcional, detalhe da missão).

Veja também: [10-area-do-heroi.md](10-area-do-heroi.md).

## Solução de problemas

### Login retorna `HTTP 404` em `/api/auth/login`

Resposta JSON típica do Spring: `"path":"/api/auth/login"`, `"status":404`.

1. **URL da API duplicada** — Se `VITE_API_BASE_URL` estiver como `http://localhost:8080/api` (ou só `/api`), o frontend chama `http://localhost:8080/api/api/auth/login`, que **não existe**. Use apenas a origem: `http://localhost:8080`, ou deixe a variável **vazia** em dev (proxy do Vite). O código em `frontend/src/config/api.ts` tenta corrigir o sufixo `/api` extra.
2. **`npm run preview` sem proxy** — Atualize o repositório: o `vite.config.ts` define proxy de `/api` também para **preview** (porta 4173). Sem isso, o POST pode não chegar ao Spring.
3. **Backend antigo ou outro processo na porta 8080** — Garanta que o JAR/`spring-boot:run` é este projeto e inclui `AuthController` (recompile: `.\mvnw.cmd clean compile` e suba de novo).
4. **Teste rápido** — No PowerShell, com o backend no ar:  
   `Invoke-WebRequest -Method POST -Uri http://localhost:8080/api/auth/login -ContentType 'application/json' -Body '{"email":"coordenacao@central-lj.demo","password":"Admin@demo2026"}'`  
   Deve retornar **200** e JSON com `accessToken` (após seed demo no banco).
