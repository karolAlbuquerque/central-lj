# Frontend — Central-LJ (React)

## Objetivo

Interface **tema operacional / heroico** (`docs/figma/style-guide.md`):

- **/** Painel (métricas, tabela, destaque ALTA/CRÍTICA) + **SSE** `/api/missions/stream` + polling 12s.
- **`/operacoes/nova`** — cria missão e abre detalhe.
- **`/missoes/:id`** — dados + **timeline** do histórico.
- **N1DevTools** (recolhível) — hello, missão teste HTTP, Kafka manual.
- Dependência **react-router-dom**.

## Pré-requisitos
- Node.js **18+**
- Backend em `http://localhost:8080` ao testar integração real

## Como rodar
```powershell
cd frontend
npm install
npm run dev
```

Abrir `http://localhost:5173`.

## Configuração da API
- **Desenvolvimento (padrão):** deixe `VITE_API_BASE_URL` **indefinida**. O Vite encaminha qualquer `fetch('/api/...')` para o backend (ver `vite.config.ts`).
- **Build estático apontando para outro host:** copie `.env.example` para `.env.local` e defina `VITE_API_BASE_URL` (ex.: `http://localhost:8080`).

Utilitário: `src/config/api.ts` (`apiUrl`).

## Estrutura `src/`
`assets`, `components`, `pages`, `services`, `hooks`, `routes`, `types`, `layouts`, `styles`, `config`.

## Build de produção
```powershell
npm run build
npm run preview
```

## Documentação do projeto
- Raiz: `../README.md`
- Plano de testes e demo: `../docs/n1/03-plano-de-testes.md`
- Entrega N1: `../docs/entrega/`
