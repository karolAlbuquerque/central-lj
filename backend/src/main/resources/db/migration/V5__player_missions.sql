-- Missões criadas por jogadores herói; separadas das missões de sistema.

CREATE TABLE player_missions (
  id           UUID PRIMARY KEY,
  titulo       VARCHAR(300) NOT NULL,
  descricao    TEXT,
  owner_user_id UUID NOT NULL REFERENCES usuarios (id),
  combat_state VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
  created_at   TIMESTAMPTZ NOT NULL,
  updated_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_player_missions_owner ON player_missions (owner_user_id);
CREATE INDEX idx_player_missions_state ON player_missions (combat_state);

-- Cooldown de duelo/troca de papel; null = sem cooldown ativo.
ALTER TABLE usuarios ADD COLUMN cooldown_available_at TIMESTAMPTZ;
