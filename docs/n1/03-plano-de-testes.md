# N1 — Plano de testes

## Objetivo do plano
Garantir, de forma **mensurável e auditável**, que a entrega N1 da Central-LJ cumpre o prometido na documentação e na arquitetura: repositório executável, integração **React ↔ Spring Boot**, prova de **publicação/consumo Kafka** em ambiente Docker, e **coerência** entre diagramas, RNFs e código — sem ainda exigir o fluxo completo de negócio da N2.

## Escopo
### Dentro do escopo (N1)
- Infraestrutura Docker (Kafka, Kafka UI, PostgreSQL provisionado localmente).
- API: `GET /api/health`, `GET /api/hello`, `POST /api/missions/test`, `POST /api/events/publish-test`.
- Consumer Kafka mínimo (evidência em log).
- Frontend: verificação de health, hello, envio de missão de teste, publicação de evento.
- Artefatos: C4, classes de domínio, RNFs, padrões, Figma (link + guia), checklist, roteiro.

### Fora do escopo (N1)
- Testes automatizados E2E completos em CI.
- Cobertura de todas as transições de `StatusMissao`.
- Persistência obrigatória em PostgreSQL ligada à aplicação.
- Autenticação, autorização e testes de carga de produção.

## Itens testáveis nesta fase
| ID | Item | Como verificar |
|----|------|----------------|
| T1 | Infra sobe | `docker compose` + `ps` + acesso Kafka UI |
| T2 | API viva | `GET /api/health` |
| T3 | Contrato hello | `GET /api/hello` JSON esperado |
| T4 | Missão de teste | `POST /api/missions/test` com `titulo` |
| T5 | Kafka publicar | `POST /api/events/publish-test` + mensagem no tópico |
| T6 | Kafka consumir | Log `[Central-LJ][Kafka]` no backend |
| T7 | Frontend integrado | Ações na home refletem T2–T5 |
| T8 | Documentação | README e `docs/n1/*` batem com endpoints e papéis do Kafka |
| T9 | Build limpo | `mvn test`, `npm run build` |

## Tipos de teste previstos
| Tipo | N1 | Futuro (N2+) |
|------|----|--------------|
| Manual guiado | Principal forma na banca / laboratório | Mantido para demos |
| Teste de contexto (Spring) | `SmokeTest` (`@SpringBootTest`) com perfil que desliga consumer | Expansão com slices |
| Teste unitário | Opcional, baixa cobertura | Services, estratégias de prioridade |
| Integração (API + Kafka + DB) | Não obrigatório | Testcontainers |
| Contrato / API | Revisão de DTO | OpenAPI + breaking change policy |
| E2E | Não obrigatório | Playwright/Cypress |

## Cenários prioritários
1. **C1 — Infraestrutura:** subir Compose; portas 9092 e 8088 acessíveis.
2. **C2 — Health:** resposta JSON coerente com serviço `central-lj-backend`.
3. **C3 — Hello:** mensagem contextual Central-LJ.
4. **C4 — Missão de teste (HTTP):** confirma recebimento e `protocolo` sem depender de Kafka.
5. **C5 — Publicação Kafka:** evento visível no Kafka UI (`missions.events` ou variável de ambiente).
6. **C6 — Consumo:** log do consumidor com payload coerente.
7. **C7 — Coerência doc ↔ código:** README e diagramas descrevem separação entre missão de teste e evento Kafka.
8. **C8 — Regressão mínima:** builds backend e frontend sem erro.

## Critérios de aceitação (N1)
- Todo cenário **C1–C8** pode ser reproduzido por um avaliador com os comandos do README.
- Ausência de contradições gritantes entre **visão**, **diagramas** e **implementação** (ex.: papel do Kafka).
- Falha de Kafka não “quebra” silenciosamente: mensagem compreensível ao operador de teste.

## Riscos
| Risco | Mitigação N1 |
|--------|----------------|
| Docker local instável | Documentar `reset.ps1` e logs; Kafka UI como prova visual |
| CORS mal configurado | Padrões de origem em dev; proxy Vite documentado |
| Confusão missão vs evento | Dois endpoints distintos + documentação explícita |
| Escopo creep para N2 | Checklist N1 e plano de testes fechados no repositório |

## Estratégia futura (N2)
- Introduzir **persistência** (PostgreSQL) e testes de integração com **Testcontainers**.
- Automatizar **transições de status** via consumidores e validar **e2e** mínimo (abrir missão → evento → estado persistido).
- Acrescentar testes de **contrato** de eventos Kafka (schema/versionamento).
- Cobrir **atualização de status** e **timeline** na UI com critérios de aceitação específicos na suíte de testes da N2.
