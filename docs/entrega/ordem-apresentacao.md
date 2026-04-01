# Ordem prática da apresentação N1 (~5 minutos)

Use este roteiro **na tela compartilhada** ou como guia de ensaio. A sequência abaixo prioriza narrativa (problema → solução → prova técnica → qualidade → próximos passos).

---

## Sequência recomendada (o que mostrar)

| # | Bloco | O que exibir / fazer | Tempo (~) |
|---|--------|----------------------|-----------|
| 1 | **Problema** | 1–2 frases + opcional: bullet “volume + rastreabilidade + gargalo síncrono” | 40 s |
| 2 | **Proposta** | “Central web distribuída”; tema Central-LJ como **metáfora operacional** (não infantilizar) | 40 s |
| 3 | **Arquitetura** | **C4 Contexto** → **C4 Contêiner** (rápido); opcional: **diagrama de classes** (só se couber) | 90 s |
| 4 | **Papel do Kafka** | Frase única: “desacopla HTTP de processamento”; **N1** = endpoint teste + consumer em log | 45 s |
| 5 | **Figma** | Abrir protótipo; mostrar **1 tela** lista/detalhe **ou** dashboard; citar **style guide** | 35 s |
| 6 | **Hello world da arquitetura** | Terminal ou browser: Compose (se já estiver no ar) → **frontend** com health + **publicar evento** → **Kafka UI** ou **log** do backend | 60 s |
| 7 | **Qualidade e testes** | Mencionar **RNFs**, **plano de testes** (C1–C8), `mvn test` / `npm run build` — sem ler o arquivo inteiro | 35 s |
| 8 | **Próximos passos N2** | Postgres no fluxo, regras nos consumidores, auth, timeline real — citar `transicao-para-n2.md` | 30 s |

**Total:** ~6 min se não cortar; **corte sugerido** se a banca for rígida: encurtar Figma (só print) ou classes (só mencionar).

---

## Versão resumida em tópicos (para slide ou cola)

1. **Problema:** muitas missões; precisa de fila e rastreio; síncrono só não escala bem.  
2. **Solução:** Central-LJ — React + Spring + Kafka + Postgres (futuro).  
3. **Arquitetura:** C4 mostra atores, SPA, API, Kafka, banco previsto.  
4. **Kafka:** eventos; N1 prova publicação + consumo mínimo.  
5. **Figma:** produto-alvo; N1 no código é demo técnica.  
6. **Demo:** health + missão teste (HTTP) + publish-test + Kafka UI/log.  
7. **Qualidade:** RNFs + plano de testes + builds.  
8. **N2:** persistência, fluxo completo, segurança.

---

## Versão em fala corrida curta (~1 min 20 s)

*Nós propomos a Central de Missões da Liga da Justiça como uma central web para registrar e acompanhar operações com ciclo de vida de missão. O desafio é conciliar resposta rápida com processamento que não trave a API quando o volume cresce — por isso adotamos uma arquitetura com React no front, Spring no back e Kafka como espinha dorsal de eventos. Na N1 entregamos documentação, diagramas C4 e de domínio, protótipo Figma com guia de estilos, e um monorepo que já sobe Kafka via Docker, integra front e back e prova publicação e consumo mínimo de mensagem. A missão de teste por HTTP é separada do evento de teste no Kafka de propósito, para deixar claro o papel da mensageria. Na N2 vamos ligar PostgreSQL ao fluxo real, implementar transições de status com consumidores e endurecer segurança e testes de integração.*

*(Use a fala longa completa em `05-roteiro-apresentacao-n1.md` se precisar de texto quase literal por ~5 min.)*

---

## Divisão de fala (grupo com 2–4 pessoas)

| Orador sugerido | Conteúdo |
|-----------------|----------|
| **Pessoa A** | Abertura, problema, proposta, stack em uma frase |
| **Pessoa B** | C4 Contexto + Contêiner (compartilhar tela); transição para Kafka |
| **Pessoa C** | Kafka (30 s) + abrir Figma (tela escolhida) |
| **Pessoa D** *(opcional)* | Demo: browser + opcional Kafka UI ou log; qualidade (RNFs/testes) e fechamento N2 |

**Regra:** uma única pessoa opera o mouse na demo para não perder tempo. **Ensaiem** a ordem exata de abas e tamanho da fonte do terminal.

---

## O que o professor costuma querer ver

- **Onde está o diagrama?** → `docs/arquitetura/*.puml` (ou PNG exportado).  
- **O sistema roda?** → README + demo em **≤ 2 minutos** após subir infra.  
- **Kafka faz o quê aqui?** → separar **HTTP** da **fila**; mostrar tópico e consumidor.  
- **Não é CRUD?** → missão como **processo + evento + estado**, não só tabela exposta.

Detalhes no roteiro estendido: `docs/n1/05-roteiro-apresentacao-n1.md`.
