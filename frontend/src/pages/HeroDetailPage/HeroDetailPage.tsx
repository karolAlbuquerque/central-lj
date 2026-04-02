import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { HeroAvailabilityBadge } from "../../components/HeroAvailabilityBadge/HeroAvailabilityBadge";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PriorityBadge } from "../../components/PriorityBadge/PriorityBadge";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { api } from "../../services/api";
import type { HeroDetail } from "../../types/mission";
import styles from "./HeroDetailPage.module.css";

export function HeroDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const isOwnProfile = user?.role === "HERO" && user.heroiId === id;
  const backTo = isOwnProfile ? "/heroi/area" : "/herois";
  const backLabel = isOwnProfile ? "← Minha área" : "← Heróis";

  const [data, setData] = useState<HeroDetail | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!id) return;
    try {
      setData(await api.getHero(id));
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar.");
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
        <Link className={styles.back} to={backTo}>
          {backLabel}
        </Link>
        <p className={styles.error}>{err}</p>
      </div>
    );
  }
  if (!data) {
    return (
      <div className={styles.page}>
        <Link className={styles.back} to={backTo}>
          {backLabel}
        </Link>
        <LoadingState message="Carregando ficha do herói…" />
      </div>
    );
  }

  const h = data.heroi;

  return (
    <div className={styles.page}>
      <Link className={styles.back} to={backTo}>
        {backLabel}
      </Link>

      <header className={styles.heroHead}>
        <div>
          <p className={styles.kicker}>Registro na central</p>
          <h1 className={styles.title}>{h.nomeHeroico}</h1>
          <p className={styles.civil}>{h.nomeCivil ? `Nome civil: ${h.nomeCivil}` : "Nome civil não informado"}</p>
        </div>
        <div className={styles.headBadges}>
          <HeroAvailabilityBadge status={h.statusDisponibilidade} />
          {!h.ativo ? <span className={styles.inactivePill}>Inativo</span> : null}
        </div>
      </header>

      <SectionCard title="Dados operacionais">
        <div className={styles.metaGrid}>
          <div>
            <span className={styles.metaLabel}>Especialidade</span>
            <p className={styles.metaValue}>{h.especialidade}</p>
          </div>
          <div>
            <span className={styles.metaLabel}>Nível</span>
            <p className={styles.metaValue}>{h.nivel}</p>
          </div>
          <div>
            <span className={styles.metaLabel}>Equipe</span>
            <p className={styles.metaValue}>
              {h.equipe ? (
                <Link className={styles.link} to={`/equipes/${h.equipe.id}`}>
                  {h.equipe.nome}
                </Link>
              ) : (
                "—"
              )}
            </p>
          </div>
        </div>
      </SectionCard>

      <SectionCard
        title="Missões como responsável direto"
        hint="Operações em que este herói foi designado individualmente."
      >
        {data.missoes.length === 0 ? (
          <p className={styles.muted}>Nenhuma missão atribuída diretamente a este herói.</p>
        ) : (
          <div className={styles.missionList}>
            {data.missoes.map((m) => (
              <div key={m.id} className={styles.missionRow}>
                <Link className={styles.missionTitle} to={`/missoes/${m.id}`}>
                  {m.titulo}
                </Link>
                <div className={styles.missionBadges}>
                  <PriorityBadge prioridade={m.prioridade} />
                  <StatusBadge status={m.status} />
                </div>
              </div>
            ))}
          </div>
        )}
      </SectionCard>
    </div>
  );
}
