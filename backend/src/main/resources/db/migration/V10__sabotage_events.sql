-- Efeitos de sabotagem aplicados por vilão após vitória em duelo.

CREATE TABLE sabotage_events (
  id                UUID PRIMARY KEY,
  duel_session_id   UUID NOT NULL REFERENCES duel_sessions (id),
  mission_id        UUID NOT NULL REFERENCES player_missions (id),
  attacker_user_id  UUID NOT NULL REFERENCES usuarios (id),
  sabotage_type     VARCHAR(50) NOT NULL,
  target_scope      VARCHAR(50) NOT NULL,
  target_user_id    UUID REFERENCES usuarios (id),
  applied_at        TIMESTAMPTZ NOT NULL,
  expires_at        TIMESTAMPTZ,
  reversed_at       TIMESTAMPTZ,
  effect_payload    JSONB
);

CREATE INDEX idx_sabotage_events_mission  ON sabotage_events (mission_id);
CREATE INDEX idx_sabotage_events_expires  ON sabotage_events (expires_at) WHERE reversed_at IS NULL;
