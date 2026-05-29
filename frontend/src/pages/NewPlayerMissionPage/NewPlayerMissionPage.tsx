import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { api } from "../../services/api";
import styles from "./NewPlayerMissionPage.module.css";

export function NewPlayerMissionPage() {
  const navigate = useNavigate();
  const [titulo, setTitulo] = useState("");
  const [descricao, setDescricao] = useState("");
  const [minPlayers, setMinPlayers] = useState(2);
  const [submitting, setSubmitting] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!titulo.trim()) return;
    setSubmitting(true);
    setErr(null);
    try {
      const mission = await api.createMission({
        titulo: titulo.trim(),
        descricao: descricao.trim() || undefined,
        minPlayers
      });
      navigate(`/missoes/${mission.id}`);
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : "Falha ao criar missão.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Nova missão"
        title="Criar missão"
        description="Crie uma missão e convide heróis para participar."
      />

      {err ? <p className={styles.error}>{err}</p> : null}

      <SectionCard title="Dados da missão">
        <form className={styles.form} onSubmit={e => { void handleSubmit(e); }}>
          <div className={styles.field}>
            <label className={styles.label} htmlFor="titulo">Título</label>
            <input
              id="titulo"
              className={styles.input}
              value={titulo}
              onChange={e => setTitulo(e.target.value)}
              placeholder="Nome da missão"
              maxLength={120}
              required
            />
          </div>
          <div className={styles.field}>
            <label className={styles.label} htmlFor="descricao">Descrição</label>
            <textarea
              id="descricao"
              className={styles.textarea}
              value={descricao}
              onChange={e => setDescricao(e.target.value)}
              placeholder="Descreva os objetivos da missão (opcional)"
              maxLength={500}
            />
          </div>
          <div className={styles.field}>
            <label className={styles.label} htmlFor="minPlayers">Mínimo de jogadores para iniciar</label>
            <input
              id="minPlayers"
              className={styles.input}
              type="number"
              min={1}
              max={8}
              value={minPlayers}
              onChange={e => setMinPlayers(Math.max(1, Number.parseInt(e.target.value || "1", 10)))}
              required
            />
          </div>
          <button type="submit" className={styles.btnSubmit} disabled={submitting || !titulo.trim()}>
            {submitting ? "Criando…" : "Criar missão"}
          </button>
        </form>
      </SectionCard>
    </div>
  );
}
