CREATE TABLE mission_history (
  id UUID PRIMARY KEY,
  mission_id UUID NOT NULL REFERENCES missions (id) ON DELETE CASCADE,
  status_anterior VARCHAR(50),
  status_novo VARCHAR(50) NOT NULL,
  mensagem TEXT,
  origem VARCHAR(50) NOT NULL,
  ocorrido_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_mission_history_mission_tempo ON mission_history (mission_id, ocorrido_em ASC);
