-- Membros de missões de jogador (chefe, heróis convidados, vilão intruso).

CREATE TABLE mission_members (
  id                  UUID PRIMARY KEY,
  mission_id          UUID NOT NULL REFERENCES player_missions (id) ON DELETE CASCADE,
  user_id             UUID NOT NULL REFERENCES usuarios (id),
  role                VARCHAR(50) NOT NULL,
  joined_at           TIMESTAMPTZ NOT NULL,
  invited_by_user_id  UUID REFERENCES usuarios (id),
  CONSTRAINT uq_mission_member UNIQUE (mission_id, user_id)
);

CREATE INDEX idx_mission_members_mission ON mission_members (mission_id);
CREATE INDEX idx_mission_members_user   ON mission_members (user_id);
