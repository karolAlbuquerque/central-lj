-- Sessões de duelo entre herói (defensor) e vilão (atacante).

CREATE TABLE duel_sessions (
  id                    UUID PRIMARY KEY,
  mission_id            UUID NOT NULL REFERENCES player_missions (id),
  attacker_user_id      UUID NOT NULL REFERENCES usuarios (id),
  defender_user_id      UUID NOT NULL REFERENCES usuarios (id),
  seed                  VARCHAR(64) NOT NULL,
  puzzle_type           VARCHAR(50) NOT NULL,
  status                VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  round_current         SMALLINT NOT NULL DEFAULT 1,
  round_max             SMALLINT NOT NULL DEFAULT 1,
  attacker_rounds_won   SMALLINT NOT NULL DEFAULT 0,
  defender_rounds_won   SMALLINT NOT NULL DEFAULT 0,
  started_at            TIMESTAMPTZ,
  finished_at           TIMESTAMPTZ,
  timeout_at            TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_duel_sessions_mission ON duel_sessions (mission_id);
CREATE INDEX idx_duel_sessions_status  ON duel_sessions (status);
