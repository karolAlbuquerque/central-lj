# N2 — Área do herói

## Objetivo

Oferecer uma experiência **enxuta e orientada ao campo**: o herói autenticado consulta suas missões designadas e o detalhe/linha do tempo, sem a carga cognitiva do painel administrativo completo.

## Rotas (frontend)

| Rota | Tela |
|------|------|
| `/heroi/area` | Boas-vindas, links rápidos para missões e perfil. |
| `/heroi/minhas-missoes` | Tabela com título, status (badge), prioridade, tipo de ameaça, última atualização; link para o detalhe. |
| `/missoes/:id` | Mesmo componente de detalhe usado na administração, com **formulário de atribuição oculto** e textos adaptados. |
| `/herois/:id` | Detalhe do **próprio** herói (alinhado ao `heroiId` do token); outros IDs retornam **403** no backend. |

## Dados

- **Lista**: `GET /api/me/missions` (token JWT do herói).
- **Detalhe / histórico**: `GET /api/missions/{id}` e `/history` apenas se a missão estiver atribuída ao herói logado (regra no backend).

## Comparação com o painel administrativo

| Aspecto | Admin | Herói |
|---------|-------|--------|
| Navegação | Painel, listas globais, cadastros, SSE opcional | Minha área + minhas missões |
| Atribuição de missão | Sim | Não (somente leitura do bloco “Atribuição”) |
| Listagem de todas as missões | Sim | Não (403 em `GET /api/missions`) |

## Documentação relacionada

- Autenticação e papéis: [09-autenticacao-e-papeis.md](09-autenticacao-e-papeis.md).
