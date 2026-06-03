# Central-LJ — Deploy em Produção

Como o sistema foi subido para o ar com serviços 100% gratuitos.

---

## Arquitetura de produção

O sistema roda em quatro serviços gerenciados distintos, cada um responsável por uma peça da stack:

```
[Usuário]
    │
    ▼
Firebase Hosting (SPA React — frontend/dist)
    │  REST + SSE
    ▼
Koyeb (Spring Boot :8080 — backend/Dockerfile)
    │                        │
    ▼                        ▼
Supabase (PostgreSQL)     Aiven (Apache Kafka)
```

| Serviço | Responsabilidade | Plano |
|---|---|---|
| **Firebase Hosting** | Serve a SPA React compilada | Spark (gratuito) |
| **Koyeb** | Executa o backend Spring Boot via Docker | Free tier |
| **Supabase** | PostgreSQL 16 gerenciado | Free tier |
| **Aiven** | Apache Kafka gerenciado com TLS/SASL | Free tier |

---

## Por que essa divisão

O backend e o frontend são deployados separadamente porque cada serviço gratuito tem uma especialidade:

- **Firebase** é otimizado para arquivos estáticos com CDN global e rewrite de SPA (`** → /index.html`)
- **Koyeb** aceita Dockerfile e roda um container Java sem custo, com URL pública HTTPS automática
- **Supabase** oferece PostgreSQL com pooler de conexões — o backend aponta para o pooler na porta 5432 com `sslmode=require`
- **Aiven** é o único serviço gratuito que oferece Kafka real com autenticação SASL_SSL — necessário porque Kafka simples sem TLS não é aceito por serviços cloud

---

## 1. Supabase (PostgreSQL)

1. Criar um projeto Free em supabase.com
2. Em **Connect → Session pooler**, copiar a URL na porta `5432`
3. No Koyeb, configurar as variáveis:

```
DATABASE_URL=jdbc:postgresql://HOST-POOLER:5432/postgres?sslmode=require
DATABASE_USER=postgres.PROJECT_REF
DATABASE_PASSWORD=SENHA_DO_BANCO
```

O backend executa as **17 migrations Flyway** automaticamente na primeira inicialização. Nenhuma configuração manual de schema é necessária.

---

## 2. Aiven (Apache Kafka)

Esta é a parte mais trabalhosa do deploy — Kafka em cloud requer autenticação mútua TLS.

1. Criar serviço **Aiven for Apache Kafka** no plano Free
2. Habilitar autenticação **SASL com SCRAM-SHA-256**
3. Criar os dois tópicos manualmente no painel Aiven:
   - `missions.created` — pipeline principal do workflow
   - `missions.events` — observabilidade e debug
4. Baixar o certificado `ca.pem` do painel Aiven
5. No Koyeb, criar um **Config File** com o conteúdo de `ca.pem` no caminho `/etc/secrets/aiven-ca.pem`
6. Configurar as variáveis no Koyeb:

```
KAFKA_BOOTSTRAP_SERVERS=HOST_AIVEN:PORTA_SASL
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=SCRAM-SHA-256
KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.scram.ScramLoginModule required username="USUARIO" password="SENHA";
KAFKA_SSL_TRUSTSTORE_TYPE=PEM
KAFKA_SSL_TRUSTSTORE_LOCATION=/etc/secrets/aiven-ca.pem
```

### Como o backend lê essas configs

O perfil `cloud` (`application-cloud.yml`) mapeia as variáveis de ambiente para as propriedades Spring Kafka:

```yaml
# application-cloud.yml
spring:
  kafka:
    properties:
      "[security.protocol]": ${KAFKA_SECURITY_PROTOCOL:SASL_SSL}
      "[sasl.mechanism]": ${KAFKA_SASL_MECHANISM:SCRAM-SHA-256}
      "[sasl.jaas.config]": ${KAFKA_SASL_JAAS_CONFIG}
      "[ssl.truststore.type]": ${KAFKA_SSL_TRUSTSTORE_TYPE:PEM}
      "[ssl.truststore.location]": ${KAFKA_SSL_TRUSTSTORE_LOCATION:/etc/secrets/aiven-ca.pem}

central-lj:
  cors:
    allowed-origin-patterns:
      - ${FRONTEND_URL}
```

O perfil base (`application.yml`) já define `bootstrap-servers` via `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}` — em produção o Koyeb injeta a variável com o host do Aiven.

---

## 3. Koyeb (Backend Spring Boot)

1. Criar um **Web Service** Free a partir do repositório GitHub
2. Apontar para o **Dockerfile em `backend/Dockerfile`**
3. Expor a porta `8080`
4. Adicionar o config file do `ca.pem` do Aiven em `/etc/secrets/aiven-ca.pem`
5. Configurar todas as variáveis de ambiente:

```
SPRING_PROFILES_ACTIVE=cloud
DATABASE_URL=jdbc:postgresql://...
DATABASE_USER=postgres.PROJECT_REF
DATABASE_PASSWORD=...
KAFKA_BOOTSTRAP_SERVERS=...
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=SCRAM-SHA-256
KAFKA_SASL_JAAS_CONFIG=...
KAFKA_SSL_TRUSTSTORE_TYPE=PEM
KAFKA_SSL_TRUSTSTORE_LOCATION=/etc/secrets/aiven-ca.pem
JWT_SECRET=SEGREDO_ALEATORIO_MIN_32_CHARS
CENTRAL_LJ_AUTH_DEMO_SEED=true
FRONTEND_URL=https://SEU_PROJETO.web.app
JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=65
```

`JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=65` limita o heap da JVM a 65% da RAM disponível no container gratuito do Koyeb, evitando OOM kill.

`SPRING_PROFILES_ACTIVE=cloud` ativa o `application-cloud.yml` que inclui as propriedades SASL/SSL do Kafka e restringe o CORS ao domínio Firebase.

### O Dockerfile do backend

```dockerfile
# Estágio 1 — build Maven
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline
COPY backend/src src
RUN ./mvnw package -DskipTests

# Estágio 2 — imagem final mínima
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --create-home --uid 10001 app
USER app
WORKDIR /app
COPY --from=build /app/target/central-lj-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Multi-stage build**: o estágio `build` usa JDK completo para compilar com Maven; o estágio final usa apenas JRE — imagem menor, sem ferramentas de build no container de produção. O usuário `app` (UID 10001) roda sem privilégios de root.

### Validação após deploy

```bash
curl https://SEU_BACKEND.koyeb.app/api/health
curl https://SEU_BACKEND.koyeb.app/actuator/health
```

Ambos devem retornar `{"status":"UP"}`.

---

## 4. Firebase Hosting (Frontend React)

### Build

O frontend precisa saber a URL pública do Koyeb antes de compilar — a variável `VITE_API_BASE_URL` é injetada em tempo de build e compilada no bundle:

```bash
cd frontend
VITE_API_BASE_URL=https://SEU_BACKEND.koyeb.app npm run build
```

Isso gera `frontend/dist/` com todos os assets estáticos.

### Como o frontend resolve a URL da API

```typescript
// config/api.ts
export function apiUrl(path: string): string {
  let base = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim() ?? "";
  base = base.replace(/\/$/, "");
  // Normaliza casos onde VITE_API_BASE_URL termina em "/api" por engano
  if (p.startsWith("/api") && (base === "/api" || base.endsWith("/api"))) {
    base = base === "/api" ? "" : base.slice(0, -"/api".length);
  }
  return `${base}${p}`;
}
```

Em dev local, `VITE_API_BASE_URL` fica vazio e o proxy do Vite (`vite.config.ts`) encaminha `/api` para `localhost:8080`. Em produção, a variável aponta para o Koyeb e o browser faz as chamadas diretamente.

### Deploy

```bash
npm install --global firebase-tools
firebase login
firebase use --add   # seleciona o projeto Firebase
firebase deploy --only hosting
```

### Configuração Firebase (`firebase.json`)

```json
{
  "hosting": {
    "public": "frontend/dist",
    "rewrites": [
      { "source": "**", "destination": "/index.html" }
    ],
    "headers": [
      {
        "source": "**/*.glb",
        "headers": [{ "key": "Cache-Control", "value": "public,max-age=86400" }]
      }
    ]
  }
}
```

O rewrite `** → /index.html` é essencial para SPA com React Router — sem ele, acessar uma rota diretamente (ex.: `/missoes/123`) retornaria 404 do Firebase.

O header de cache nos arquivos `.glb` (modelos 3D) coloca-os em cache por 24h, evitando redownload dos assets pesados a cada visita.

### CORS no backend

Com o frontend em Firebase e o backend no Koyeb (domínios diferentes), o CORS precisa liberar explicitamente a origem Firebase. O perfil `cloud` lê isso da variável `FRONTEND_URL`:

```yaml
central-lj:
  cors:
    allowed-origin-patterns:
      - ${FRONTEND_URL}   # ex: https://central-lj-xyz.web.app
```

Se a URL do Firebase mudar após o deploy, é necessário atualizar `FRONTEND_URL` no Koyeb e fazer redeploy do backend.

---

## Alternativa: docker-compose.prod.yml (auto-hospedado)

Para subir tudo em um servidor próprio (VPS, máquina em nuvem), o projeto tem um `infra/docker-compose.prod.yml` que sobe todos os serviços em uma rede Docker interna:

```
frontend (Nginx :80) → backend (:8080) → postgres (:5432 interno)
                                        → kafka (:9092 interno)
```

O Nginx serve a SPA e faz proxy reverso de `/api/` e `/ws` para o backend — o frontend nunca fica exposto diretamente na rede externa.

```yaml
# nginx/default.conf (resumido)
location /api/ {
    proxy_pass http://backend:8080;
    proxy_buffering off;   # necessário para SSE funcionar
    proxy_cache off;
    proxy_read_timeout 1h; # SSE é conexão longa
}

location /ws {
    proxy_pass http://backend:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}

location / {
    try_files $uri $uri/ /index.html; # SPA fallback
}
```

`proxy_buffering off` e `proxy_read_timeout 1h` são críticos para SSE — sem eles o Nginx fecha a conexão ou bufferiza os eventos antes de entregar ao browser.

O Kafka nesse compose usa **KRaft mode** (sem ZooKeeper), com 3 partições por padrão e volumes persistentes:

```yaml
kafka:
  image: apache/kafka:3.7.2
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_NUM_PARTITIONS: 3
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
  volumes:
    - kafka_prod_data:/tmp/kraft-combined-logs
```

Para subir:

```bash
cp infra/.env.prod.example infra/.env.prod
# Editar .env.prod com as senhas
docker compose --env-file infra/.env.prod -f infra/docker-compose.prod.yml up -d --build
```

---

## Resumo dos perfis de configuração

| Perfil | Banco | Kafka | Quando usar |
|---|---|---|---|
| `(padrão)` | PostgreSQL local :5433 | localhost:9092 sem TLS | Desenvolvimento com Docker |
| `local` | H2 em memória | desabilitado | Testes rápidos sem Docker |
| `cloud` | Supabase (SSL) | Aiven (SASL_SSL) | Produção no Koyeb |
| `test` | H2 em memória | desabilitado | Testes automatizados (CI) |

---

## Smoke test pós-deploy

1. Abrir o frontend no Firebase
2. Fazer login com a conta demo de coordenação
3. Criar uma nova missão
4. Observar o status evoluir automaticamente na tela (SSE + workflow Kafka)
5. Verificar `GET /api/health` e `GET /actuator/health` retornando `UP`
