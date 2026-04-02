-- Equipes heroicas e heróis; atribuição opcional em missões (MVP Central-LJ).

CREATE TABLE equipes_heroicas (
  id UUID PRIMARY KEY,
  nome VARCHAR(200) NOT NULL,
  especialidade_principal VARCHAR(200),
  ativa BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE heroes (
  id UUID PRIMARY KEY,
  nome_heroico VARCHAR(200) NOT NULL,
  nome_civil VARCHAR(200),
  especialidade VARCHAR(200) NOT NULL,
  status_disponibilidade VARCHAR(50) NOT NULL,
  nivel VARCHAR(50) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  equipe_id UUID REFERENCES equipes_heroicas (id),
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_heroes_equipe ON heroes (equipe_id);
CREATE INDEX idx_heroes_disponibilidade ON heroes (status_disponibilidade);

ALTER TABLE missions ADD COLUMN heroi_responsavel_id UUID REFERENCES heroes (id);
ALTER TABLE missions ADD COLUMN equipe_responsavel_id UUID REFERENCES equipes_heroicas (id);
ALTER TABLE missions ADD COLUMN atribuido_em TIMESTAMPTZ;
ALTER TABLE missions ADD COLUMN atribuido_por VARCHAR(200);

CREATE INDEX idx_missions_heroi ON missions (heroi_responsavel_id);
CREATE INDEX idx_missions_equipe ON missions (equipe_responsavel_id);
