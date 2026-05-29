-- Tarefas de missões de jogador; tarefas críticas exigem puzzle.

CREATE TABLE mission_tasks (
  id                    UUID PRIMARY KEY,
  mission_id            UUID NOT NULL REFERENCES player_missions (id) ON DELETE CASCADE,
  assigned_to_user_id   UUID REFERENCES usuarios (id),
  title                 VARCHAR(200) NOT NULL,
  description           TEXT,
  status                VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  is_critical           BOOLEAN NOT NULL DEFAULT FALSE,
  depends_on_task_id    UUID REFERENCES mission_tasks (id),
  created_at            TIMESTAMPTZ NOT NULL,
  updated_at            TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_mission_tasks_mission ON mission_tasks (mission_id);
CREATE INDEX idx_mission_tasks_assignee ON mission_tasks (assigned_to_user_id);
