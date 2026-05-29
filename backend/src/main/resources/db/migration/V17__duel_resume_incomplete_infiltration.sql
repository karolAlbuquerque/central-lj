-- Duelos criados antes da fase INFILTRATING: PENDING com progresso < 3 devem retomar a brecha.
UPDATE duel_sessions
SET status = 'INFILTRATING'
WHERE status = 'PENDING'
  AND infiltration_progress < 3;
