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

export type MissionHistoryOrigin = "API_REGISTRO" | "KAFKA_WORKFLOW" | "KAFKA_WORKFLOW_ERRO";

export type LocalizacaoResponse = {
  cidade: string | null;
  bairro: string | null;
  referencia: string | null;
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
