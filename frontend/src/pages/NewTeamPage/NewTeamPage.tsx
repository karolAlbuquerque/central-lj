import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, type CreateTeamPayload } from "../../services/api";
import styles from "../NewHeroPage/NewHeroPage.module.css";

export function NewTeamPage() {
  const navigate = useNavigate();
  const [nome, setNome] = useState("");
  const [esp, setEsp] = useState("");
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!nome.trim()) {
      setErr("Informe o nome da equipe.");
      return;
    }
    const payload: CreateTeamPayload = {
      nome: nome.trim(),
      especialidadePrincipal: esp.trim() || null,
      ativa: true
    };
    setBusy(true);
    setErr(null);
    try {
      const t = await api.createTeam(payload);
      navigate(`/equipes/${t.id}`);
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : "Falha.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={styles.page}>
      <Link className={styles.back} to="/equipes">
        ← Equipes
      </Link>
      <h1 className={styles.title}>Nova equipe</h1>
      <form className={styles.card} onSubmit={onSubmit}>
        <div className={styles.row}>
          <label htmlFor="nm">Nome *</label>
          <input
            id="nm"
            className={styles.input}
            value={nome}
            onChange={(e) => setNome(e.target.value)}
          />
        </div>
        <div className={styles.row}>
          <label htmlFor="ep">Especialidade principal</label>
          <input
            id="ep"
            className={styles.input}
            value={esp}
            onChange={(e) => setEsp(e.target.value)}
          />
        </div>
        {err && <p className={styles.error}>{err}</p>}
        <button className={styles.btn} type="submit" disabled={busy}>
          {busy ? "Salvando…" : "Criar equipe"}
        </button>
      </form>
    </div>
  );
}
