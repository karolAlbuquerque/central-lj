export type MissionTestPayload = {
  titulo: string;
  descricao?: string;
};

export type MissionStatus =
  | "RECEBIDA"
  | "EM_ANALISE"
  | "PRIORIZADA"
  | "EQUIPE_DESIGNADA"
  | "EM_ANDAMENTO"
  | "CONCLUIDA"
  | "FALHA_PROCESSAMENTO";

export type TipoAmeaca =
  | "INVASAO"
  | "DESASTRE_NATURAL"
  | "META_HUMANO"
  | "TECNOLOGICA"
  | "DESCONHECIDA";

export type PrioridadeMissao = "BAIXA" | "MEDIA" | "ALTA" | "CRITICA";

export type MissionHistoryOrigin =
  | "API_REGISTRO"
  | "API_ATRIBUICAO"
  | "KAFKA_WORKFLOW"
  | "KAFKA_WORKFLOW_ERRO";

export type LocalizacaoResponse = {
  cidade: string | null;
  bairro: string | null;
  referencia: string | null;
};

export type AtribuicaoResumo = {
  heroiId: string | null;
  nomeHeroico: string | null;
  equipeId: string | null;
  nomeEquipe: string | null;
  atribuidoEm: string | null;
  atribuidoPor: string | null;
};

export type Mission = {
  id: string;
  titulo: string;
  descricao: string | null;
  tipoAmeaca: TipoAmeaca;
  prioridade: PrioridadeMissao;
  status: MissionStatus;
  dataCriacao: string;
  ultimaAtualizacao: string;
  localizacao: LocalizacaoResponse | null;
  atribuicao: AtribuicaoResumo | null;
};

export type MissionHistoryEntry = {
  id: string;
  statusAnterior: MissionStatus | null;
  statusNovo: MissionStatus;
  mensagem: string | null;
  origem: MissionHistoryOrigin;
  ocorridoEm: string;
};

export type MissionDetail = {
  missao: Mission;
  historico: MissionHistoryEntry[];
};

export type DashboardSummary = {
  totalMissaoes: number;
  emAndamento: number;
  concluidas: number;
  falhas: number;
  recentes: Mission[];
};

export type CreateMissionPayload = {
  titulo: string;
  descricao?: string | null;
  tipoAmeaca: TipoAmeaca;
  prioridade: PrioridadeMissao;
  cidade?: string | null;
  bairro?: string | null;
  referencia?: string | null;
};

export type HeroiDisponibilidade = "DISPONIVEL" | "EM_MISSAO" | "INATIVO";

export type EquipeResumo = { id: string; nome: string };

export type Hero = {
  id: string;
  nomeHeroico: string;
  nomeCivil: string | null;
  especialidade: string;
  statusDisponibilidade: HeroiDisponibilidade;
  nivel: string;
  ativo: boolean;
  equipe: EquipeResumo | null;
  createdAt: string;
  updatedAt: string;
};

export type HeroDetail = {
  heroi: Hero;
  missoes: Mission[];
};

export type Equipe = {
  id: string;
  nome: string;
  especialidadePrincipal: string | null;
  ativa: boolean;
  createdAt: string;
  updatedAt: string;
};

export type EquipeDetail = {
  equipe: Equipe;
  membros: Hero[];
};
