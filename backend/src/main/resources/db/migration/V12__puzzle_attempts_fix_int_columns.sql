-- Corrige tipo da coluna de rodada: SMALLINT → INTEGER para alinhar ao mapeamento JPA.
ALTER TABLE puzzle_attempts
  ALTER COLUMN round_number TYPE INTEGER;
