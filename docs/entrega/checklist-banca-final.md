# Checklist — banca final N2 (Central-LJ)

Marque antes de entrar na sala. **Ordem recomendada:** infra → backend → frontend → demo rápida.

## Ambiente

- [ ] Docker Desktop (ou engine) **rodando**
- [ ] `.\infra\scripts\up.ps1` executado **sem erro**
- [ ] http://localhost:8088 (Kafka UI) **abre**
- [ ] Postgres aceita conexão na **5433** (ou perfil `local` / H2 como plano B)

## Serviços

- [ ] **Kafka** escutando em `localhost:9092`
- [ ] **PostgreSQL** `central_lj` / usuário `central_lj`

## Aplicação

- [ ] Backend sobe: `cd backend` → `.\mvnw.cmd spring-boot:run` (ou `mvn spring-boot:run` se Maven estiver no PATH)
- [ ] `GET http://localhost:8080/api/health` → **200**
- [ ] Frontend sobe: `cd frontend` → `npm install` (se necessário) → `npm run dev`
- [ ] http://localhost:5173 **abre** o painel

## Fluxo funcional

- [ ] **Nova missão** cria e redireciona ao detalhe
- [ ] Timeline recebe entradas após o consumer (aguardar alguns segundos)
- [ ] **Painel:** métricas coerentes com o banco
- [ ] **Filtros** (Recentes / Recebidas / Concluídas / Falhas / Em análise) retornam listas
- [ ] **SSE** ou, na falha dele, **polling** atualiza a tela em até ~12s

## Kafka (opcional na hora, mas recomendado)

- [ ] Após criar missão, há mensagem no tópico **`missions.created`** (Kafka UI)

## Testes

- [ ] `cd backend` → `.\mvnw.cmd test` → **BUILD SUCCESS**
- [ ] (Opcional) Abrir `backend/target/site/jacoco/index.html` se gerado

## Documentação / links

- [ ] `README.md` raiz revisado
- [ ] `docs/n2/*` e `docs/entrega/roteiro-demo-final.md` acessíveis
- [ ] Link Figma (`docs/figma/link-figma.md`) válido para o time

## Plano B (se algo falhar)

- [ ] Saber usar **`.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local`** (H2, sem Postgres)
- [ ] Saber explicar demo **só backend + Postman** se frontend não subir
