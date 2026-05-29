-- Tentativas de puzzle por participante por round de duelo.

CREATE TABLE puzzle_attempts (
  id               UUID PRIMARY KEY,
  duel_session_id  UUID NOT NULL REFERENCES duel_sessions (id),
  user_id          UUID NOT NULL REFERENCES usuarios (id),
  round_number     SMALLINT NOT NULL,
  move_sequence    JSONB,
  is_valid         BOOLEAN,
  time_ms          INTEGER,
  submitted_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_puzzle_attempts_duel ON puzzle_attempts (duel_session_id);
