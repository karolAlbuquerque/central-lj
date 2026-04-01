CREATE TABLE missions (
  id UUID PRIMARY KEY,
  titulo VARCHAR(500) NOT NULL,
  descricao TEXT,
  tipo_ameaca VARCHAR(50) NOT NULL,
  prioridade VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  data_criacao TIMESTAMPTZ NOT NULL,
  ultima_atualizacao TIMESTAMPTZ NOT NULL,
  cidade VARCHAR(200),
  bairro VARCHAR(200),
  referencia VARCHAR(500)
);

CREATE INDEX idx_missions_status ON missions (status);
CREATE INDEX idx_missions_data_criacao ON missions (data_criacao DESC);
