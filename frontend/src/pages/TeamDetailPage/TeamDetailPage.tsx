import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { HeroAvailabilityBadge } from "../../components/HeroAvailabilityBadge/HeroAvailabilityBadge";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { api } from "../../services/api";
import type { EquipeDetail } from "../../types/mission";
import styles from "./TeamDetailPage.module.css";

export function TeamDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [data, setData] = useState<EquipeDetail | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!id) return;
    try {
      setData(await api.getTeam(id));
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha.");
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);

  if (!id) {
    return <p className={styles.error}>ID inválido.</p>;
  }
  if (err) {
    return (
      <div className={styles.page}>
        <Link className={styles.back} to="/equipes">
          ← Equipes
        </Link>
        <p className={styles.error}>{err}</p>
      </div>
    );
  }
  if (!data) {
    return (
      <div className={styles.page}>
        <Link className={styles.back} to="/equipes">
          ← Equipes
        </Link>
        <LoadingState message="Carregando unidade…" />
      </div>
    );
  }

  const e = data.equipe;

  return (
    <div className={styles.page}>
      <Link className={styles.back} to="/equipes">
        ← Equipes
      </Link>

      <header className={styles.head}>
        <p className={styles.kicker}>Equipe tática</p>
        <h1 className={styles.title}>{e.nome}</h1>
        <div className={styles.meta}>
          <div>
            <strong>Especialidade</strong> {e.especialidadePrincipal ?? "—"}
          </div>
          <div>
            <strong>Status</strong> {e.ativa ? "Ativa" : "Inativa"}
          </div>
        </div>
      </header>

      <section className={styles.panel}>
        <h2 className={styles.sectionTitle}>Membros vinculados</h2>
        {data.membros.length === 0 ? (
          <p className={styles.muted}>Nenhum herói vinculado a esta equipe.</p>
        ) : (
          <ul className={styles.memberList}>
            {data.membros.map((h) => (
              <li key={h.id}>
                <Link className={styles.link} to={`/herois/${h.id}`}>
                  {h.nomeHeroico}
                </Link>
                <HeroAvailabilityBadge status={h.statusDisponibilidade} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
