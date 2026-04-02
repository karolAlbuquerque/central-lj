import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, type CreateHeroPayload } from "../../services/api";
import type { Equipe, HeroiDisponibilidade } from "../../types/mission";
import styles from "./NewHeroPage.module.css";

export function NewHeroPage() {
  const navigate = useNavigate();
  const [teams, setTeams] = useState<Equipe[]>([]);
  const [nomeHeroico, setNomeHeroico] = useState("");
  const [nomeCivil, setNomeCivil] = useState("");
  const [especialidade, setEspecialidade] = useState("");
  const [nivel, setNivel] = useState("A");
  const [disp, setDisp] = useState<HeroiDisponibilidade>("DISPONIVEL");
  const [equipeId, setEquipeId] = useState("");
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    void api.listTeams().then(setTeams).catch(() => {});
  }, []);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setErr(null);
    if (!nomeHeroico.trim() || !especialidade.trim()) {
      setErr("Nome heroico e especialidade são obrigatórios.");
      return;
    }
    const payload: CreateHeroPayload = {
      nomeHeroico: nomeHeroico.trim(),
      nomeCivil: nomeCivil.trim() || null,
      especialidade: especialidade.trim(),
      statusDisponibilidade: disp,
      nivel: nivel.trim(),
      ativo: true,
      equipeId: equipeId || null
    };
    setBusy(true);
    try {
      const h = await api.createHero(payload);
      navigate(`/herois/${h.id}`);
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : "Falha ao salvar.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={styles.page}>
      <Link className={styles.back} to="/herois">
        ← Heróis
      </Link>
      <h1 className={styles.title}>Cadastrar herói</h1>
      <form className={styles.card} onSubmit={onSubmit}>
        <div className={styles.row}>
          <label htmlFor="nh">Nome heroico *</label>
          <input
            id="nh"
            className={styles.input}
            value={nomeHeroico}
            onChange={(e) => setNomeHeroico(e.target.value)}
          />
        </div>
        <div className={styles.row}>
          <label htmlFor="nc">Nome civil</label>
          <input
            id="nc"
            className={styles.input}
            value={nomeCivil}
            onChange={(e) => setNomeCivil(e.target.value)}
          />
        </div>
        <div className={styles.row}>
          <label htmlFor="es">Especialidade *</label>
          <input
            id="es"
            className={styles.input}
            value={especialidade}
            onChange={(e) => setEspecialidade(e.target.value)}
          />
        </div>
        <div className={styles.row}>
          <label htmlFor="nv">Nível</label>
          <input
            id="nv"
            className={styles.input}
            value={nivel}
            onChange={(e) => setNivel(e.target.value)}
            placeholder="ex: S, A, B"
          />
        </div>
        <div className={styles.row}>
          <label htmlFor="dp">Disponibilidade inicial</label>
          <select
            id="dp"
            className={styles.select}
            value={disp}
            onChange={(e) => setDisp(e.target.value as HeroiDisponibilidade)}
          >
            <option value="DISPONIVEL">Disponível</option>
            <option value="EM_MISSAO">Em missão</option>
            <option value="INATIVO">Inativo</option>
          </select>
        </div>
        <div className={styles.row}>
          <label htmlFor="eq">Equipe (opcional)</label>
          <select
            id="eq"
            className={styles.select}
            value={equipeId}
            onChange={(e) => setEquipeId(e.target.value)}
          >
            <option value="">— Nenhuma —</option>
            {teams.map((t) => (
              <option key={t.id} value={t.id}>
                {t.nome}
              </option>
            ))}
          </select>
        </div>
        {err && <p className={styles.error}>{err}</p>}
        <button className={styles.btn} type="submit" disabled={busy}>
          {busy ? "Salvando…" : "Salvar herói"}
        </button>
      </form>
    </div>
  );
}
