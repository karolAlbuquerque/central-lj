-- Corrige tipo das colunas de contagem: SMALLINT → INTEGER para alinhar ao mapeamento JPA.
ALTER TABLE duel_sessions
  ALTER COLUMN round_current       TYPE INTEGER,
  ALTER COLUMN round_max           TYPE INTEGER,
  ALTER COLUMN attacker_rounds_won TYPE INTEGER,
  ALTER COLUMN defender_rounds_won TYPE INTEGER;
