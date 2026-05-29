-- Alinha infiltration_progress ao mapeamento JPA (int → INTEGER), igual V11 nas demais colunas.
ALTER TABLE duel_sessions
  ALTER COLUMN infiltration_progress TYPE INTEGER;
