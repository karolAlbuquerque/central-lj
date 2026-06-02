# Deploy gratuito gerenciado

Arquitetura preservada: Firebase Hosting serve a SPA React; Koyeb executa a API Spring Boot;
Supabase fornece PostgreSQL; Aiven fornece Apache Kafka.

## 1. Supabase

1. Crie um projeto Free.
2. Em **Connect**, copie a URL do **Session pooler** na porta `5432`.
3. No Koyeb, use:

```text
DATABASE_URL=jdbc:postgresql://HOST-DO-POOLER:5432/postgres?sslmode=require
DATABASE_USER=postgres.PROJECT_REF
DATABASE_PASSWORD=SENHA_DO_BANCO
```

O backend executa as migrations Flyway automaticamente na primeira inicialização.

## 2. Aiven Kafka

1. Crie um serviço **Aiven for Apache Kafka Free**.
2. Habilite autenticação SASL e use `SCRAM-SHA-256`.
3. Crie os tópicos `missions.created` e `missions.events`.
4. Baixe o certificado CA `ca.pem`.
5. No Koyeb, adicione um config file com o conteúdo de `ca.pem` no caminho
   `/etc/secrets/aiven-ca.pem`.
6. Configure:

```text
KAFKA_BOOTSTRAP_SERVERS=HOST_AIVEN:PORTA_SASL
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=SCRAM-SHA-256
KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.scram.ScramLoginModule required username="USUARIO" password="SENHA";
KAFKA_SSL_TRUSTSTORE_TYPE=PEM
KAFKA_SSL_TRUSTSTORE_LOCATION=/etc/secrets/aiven-ca.pem
```

## 3. Koyeb

1. Crie um Web Service Free a partir do repositório GitHub.
2. Use o Dockerfile `backend/Dockerfile`.
3. Exponha a porta HTTP `8080`.
4. Além das variáveis do Supabase e Aiven, configure:

```text
SPRING_PROFILES_ACTIVE=cloud
JWT_SECRET=SEGREDO_ALEATORIO_COM_PELO_MENOS_32_CARACTERES
CENTRAL_LJ_AUTH_DEMO_SEED=true
FRONTEND_URL=https://SEU_PROJETO.web.app
JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=65
```

Use `CENTRAL_LJ_AUTH_DEMO_SEED=true` somente para a publicação demonstrativa. Para dados reais,
use `false`.

Depois do deploy, valide:

```bash
curl https://SEU_BACKEND.koyeb.app/api/health
curl https://SEU_BACKEND.koyeb.app/actuator/health
```

## 4. Firebase Hosting

1. Crie um projeto Firebase no plano Spark.
2. Gere o build apontando para a URL pública do Koyeb:

```bash
cd frontend
VITE_API_BASE_URL=https://SEU_BACKEND.koyeb.app npm run build
cd ..
```

3. Instale e autentique o Firebase CLI, caso necessário:

```bash
npm install --global firebase-tools
firebase login
firebase use --add
```

4. Publique:

```bash
firebase deploy --only hosting
```

5. Se a URL final do Firebase diferir da configurada em `FRONTEND_URL`, atualize essa variável
   no Koyeb e faça um novo deploy do backend.

## 5. Smoke test

1. Abra o frontend Firebase.
2. Entre com a conta demo de coordenação.
3. Crie uma missão.
4. Confirme que o status evolui pelo workflow Kafka.
5. Confira o endpoint `/api/health` e os logs do Koyeb.

