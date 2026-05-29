export type MissionCombatState =
  | "LOBBY"
  | "ACTIVE"
  | "NORMAL"
  | "ALERTA_INFILTRACAO"
  | "EM_DUELO"
  | "SABOTADA"
  | "DEFENDIDA"
  | "SEM_CHEFE"
  | "EM_CRISE"
  | "COMPROMETIDA";

export type MissionMemberRole = "CHEFE" | "HERO_MEMBER" | "VILLAIN_INTRUDER";
export type MissionInviteStatus = "PENDING" | "ACCEPTED" | "DECLINED";

export type TaskStatus = "PENDING" | "IN_PROGRESS" | "AWAITING_PUZZLE" | "DONE" | "BLOCKED";

export type DuelStatus =
  | "INFILTRATING"
  | "PENDING"
  | "ACTIVE"
  | "HERO_WON"
  | "VILLAIN_WON"
  | "CANCELLED"
  | "TIMEOUT";

export type PuzzleType =
  | "DRAG_SORT"
  | "NODE_CONNECT"
  | "SEQUENCE_INPUT"
  | "CARD_MATCH"
  | "TILE_ROTATE"
  | "COLOR_SIMON"
  | "TERMINAL_HACK"
  | "SLIDING_TILE"
  | "ARCADE_SHOOTER"
  | "ARCADE_DODGE";

export type SabotageType = "BLOCK_TASK" | "SLOW_PROGRESS" | "REMOVE_MEMBER" | "SILENT_OBSERVE";

export type SabotageScope = "CHEFE" | "MEMBRO_ESPECIFICO" | "EQUIPE" | "SILENCIOSO";

export type Mission = {
  id: string;
  titulo: string;
  descricao: string | null;
  ownerUserId: string;
  combatState: MissionCombatState;
  minPlayers: number;
  partySeed: string | null;
  startedByUserId: string | null;
  startedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type MissionMember = {
  id: string;
  missionId: string;
  userId: string;
  userName: string;
  role: MissionMemberRole;
  inviteStatus: MissionInviteStatus;
  joinedAt: string;
};

export type MissionTask = {
  id: string;
  missionId: string;
  assignedToUserId: string | null;
  title: string;
  description: string | null;
  status: TaskStatus;
  critical: boolean;
  dependsOnTaskId: string | null;
  createdAt: string;
  updatedAt: string;
  canExecute: boolean;
  puzzleSeed: string | null;
  puzzleType: PuzzleType | null;
};

export type TaskAction = "START" | "COMPLETE" | "SUBMIT_PUZZLE";

export type ExecuteTaskPayload = {
  action: TaskAction;
  moves?: number[];
};

export type MissionDetail = {
  mission: Mission;
  members: MissionMember[];
  tasks: MissionTask[];
};

export type DuelSession = {
  id: string;
  missionId: string;
  attackerUserId: string;
  defenderUserId: string;
  seed: string;
  puzzleType: PuzzleType;
  status: DuelStatus;
  roundCurrent: number;
  roundMax: number;
  attackerRoundsWon: number;
  defenderRoundsWon: number;
  infiltrationProgress: number;
  infiltrationRequired: number;
  startedAt: string | null;
  finishedAt: string | null;
  timeoutAt: string;
};

export type CreateMissionPayload = {
  titulo: string;
  descricao?: string | null;
  minPlayers?: number;
};

export type CreateTaskPayload = {
  title: string;
  description?: string | null;
  assignedToUserId?: string | null;
  critical: boolean;
  puzzleType?: PuzzleType | null;
  dependsOnTaskId?: string | null;
};

export type PuzzleProgressPayload = {
  moves: number[];
  roundNumber: number;
  timeMs: number;
};
