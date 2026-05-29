ALTER TABLE player_missions
  ADD COLUMN min_players INT NOT NULL DEFAULT 1,
  ADD COLUMN party_seed VARCHAR(64),
  ADD COLUMN started_by_user_id UUID REFERENCES usuarios (id),
  ADD COLUMN started_at TIMESTAMPTZ;

ALTER TABLE mission_members
  ADD COLUMN invite_status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED';

CREATE INDEX idx_mission_members_invite_status ON mission_members (mission_id, invite_status);
