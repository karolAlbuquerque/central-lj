# Figma — Link, escopo e apresentação para o professor

## 1. Onde colar o link definitivo

Preencha a tabela abaixo **antes** da entrega ou envio ao docente. Remova linhas de exemplo se preferir um README mais “limpo” após colar a URL.

| Campo | Valor |
|--------|--------|
| **URL do Figma (protótipo navegável)** | `https://www.figma.com/design/` _…cole o link completo…_ |
| **Modo de compartilhamento** | _Ex.: “Qualquer pessoa com o link pode visualizar” ou convite por e-mail_ |
| **Nome do arquivo** | _Ex.: Central-LJ — operação e dashboard_ |
| **Última atualização** | _DD/MM/AAAA_ |
| **Responsável(is)** | _Nomes da equipe_ |

**Se o arquivo for privado:** indique na entrega como o professor obtém acesso (convite, equipe educacional, export PDF de backup).

---

## 2. O que mostrar ao professor (ordem sugerida)

1. **Capa / frame inicial** com o nome **Central-LJ** ou **Central de Missões da Liga da Justiça** e identidade alinhada ao [style-guide.md](style-guide.md).
2. **Fluxo principal clicável** (protótipo): pelo menos **lista de missões → detalhe com timeline** *ou* **nova missão → confirmação**.
3. **Contraste de telas:** uma tela **operador** (lista / tarefa) e um **dashboard admin** (visão agregada) — mesmo que o admin seja só um frame.
4. **Estado vazio ou erro** (1 frame) — prova que a equipe pensou além do caminho feliz.
5. **Comentário rápido (viva-voz):** “A N1 no GitHub é **demo técnica**; o Figma é o **alvo de produto** para a N2.”

---

## 3. Checklist — telas mínimas esperadas

Marque ao concluir no Figma:

- [ ] **Login ou entrada** (pode ser placeholder; alinha expectativa de auth na N2).
- [ ] **Home operacional** — atalhos ou resumo (“Nova missão”, “Minhas missões”).
- [ ] **Lista de missões** — filtros por status, prioridade ou tipo de ameaça (pelo menos indicados na UI).
- [ ] **Detalhe da missão** com **timeline / marcos de status** (conceito central do domínio).
- [ ] **Formulário “Nova missão”** — campos coerentes com o diagrama de classes (título, descrição, local, ameaça, prioridade).
- [ ] **Dashboard administrativo** — KPIs ou blocos de volume (mesmo com dados fictícios).
- [ ] **Empty state** e **erro** (mock).
- [ ] **Protótipo:** fluxo principal **sem telas órfãs** (links de prototype entre frames-chave).

---

## 4. Checklist — consistência visual (antes da banca)

- [ ] Paleta do **style guide** (fundo escuro, acentos definidos).
- [ ] No máximo **duas famílias** tipográficas principais (+ monoespaçada opcional para dados).
- [ ] Status da missão: **cor + texto ou ícone** (nunca só cor).
- [ ] Hierarquia de botões: primário / secundário / perigo.
- [ ] Espaçamento em múltiplos de **4 ou 8 px** entre blocos.
- [ ] Anotações curtas no Figma em 1–2 frames críticos (decisão de UX em uma frase).

---

## 5. Alinhamento com o repositório

| Repositório | Figma |
|-------------|--------|
| `docs/figma/style-guide.md` | Tokens, componentes, admin vs operador |
| `docs/arquitetura/diagrama-classes.puml` | Conceitos exibidos nas telas (missão, status, evento) |
| `frontend/` (N1) | Demonstração técnica — **não substitui** o protótipo completo |
