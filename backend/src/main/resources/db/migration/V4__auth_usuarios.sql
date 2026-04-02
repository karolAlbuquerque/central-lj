-- Contas operacionais: papéis e vínculo opcional com herói (MVP autenticação Central-LJ).

CREATE TABLE usuarios (
  id UUID PRIMARY KEY,
  nome VARCHAR(200) NOT NULL,
  email VARCHAR(320) NOT NULL,
  senha_hash VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  heroi_id UUID REFERENCES heroes (id),
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uq_usuarios_email UNIQUE (email),
  CONSTRAINT chk_usuario_hero_papel CHECK (
    (role <> 'HERO') OR (heroi_id IS NOT NULL)
  ),
  CONSTRAINT chk_usuario_admin_sem_heroi CHECK (
    (role <> 'ADMIN') OR (heroi_id IS NULL)
  )
);

CREATE INDEX idx_usuarios_email ON usuarios (lower(email));
