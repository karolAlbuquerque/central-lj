import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { HeroAvailabilityBadge } from "../../components/HeroAvailabilityBadge/HeroAvailabilityBadge";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { PriorityBadge } from "../../components/PriorityBadge/PriorityBadge";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatCard } from "../../components/StatCard/StatCard";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { api } from "../../services/api";
import type { HeroDetail, Mission } from "../../types/mission";
import styles from "./HeroAreaPage.module.css";

const DONE: Mission["status"] = "CONCLUIDA";

function sortUrgent(a: Mission, b: Mission): number {
  const p = { CRITICA: 0, ALTA: 1, MEDIA: 2, BAIXA: 3 };
  const d = p[a.prioridade] - p[b.prioridade];
  if (d !== 0) return d;
  return new Date(a.ultimaAtualizacao).getTime() - new Date(b.ultimaAtualizacao).getTime();
}

export function HeroAreaPage() {
  const { user } = useAuth();
  const [detail, setDetail] = useState<HeroDetail | null>(null);
  const [missions, setMissions] = useState<Mission[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    setErr(null);
    try {
      const my = await api.listMyMissions();
      setMissions(my);
      if (user?.heroiId) {
        try {
          setDetail(await api.getHero(user.heroiId));
        } catch {
          setDetail(null);
        }
      } else {
        setDetail(null);
      }
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar.");
    } finally {
      setLoading(false);
    }
  }, [user?.heroiId]);

  useEffect(() => {
    void load();
  }, [load]);

  const open = missions.filter((m) => m.status !== DONE);
  const done = missions.filter((m) => m.status === DONE);
  const urgent = [...open].sort(sortUrgent)[0];

  const nomeHeroico = detail?.heroi.nomeHeroico;

  if (loading) {
    return <LoadingState message="Sincronizando sua área operacional…" />;
  }

  if (err) {
    return <p className={styles.error}>{err}</p>;
  }

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Console do herói"
        title={nomeHeroico ? `${nomeHeroico}` : `Olá, ${user?.nome ?? "operador"}`}
        description={
          <>
            Acompanhe operações designadas a você. Somente missões em que você é o{" "}
            <strong>responsável atual</strong> aparecem aqui. Atualizações em tempo quase real via fluxo da
            central.
          </>
        }
        actions={
          <Link className={styles.btnPrimary} to="/heroi/minhas-missoes">
            Minhas missões
          </Link>
        }
      />

      {detail ? (
        <div className={styles.profileStrip}>
          <div>
            <span className={styles.stripLabel}>Disponibilidade</span>
            <HeroAvailabilityBadge status={detail.heroi.statusDisponibilidade} />
          </div>
          <div>
            <span className={styles.stripLabel}>Equipe</span>
            <span className={styles.stripValue}>
              {detail.heroi.equipe ? (
                <Link className={styles.link} to={`/equipes/${detail.heroi.equipe.id}`}>
                  {detail.heroi.equipe.nome}
                </Link>
              ) : (
                "—"
              )}
            </span>
          </div>
          {user?.heroiId ? (
            <Link className={styles.btnGhost} to={`/herois/${user.heroiId}`}>
              Ficha completa
            </Link>
          ) : null}
        </div>
      ) : null}

      <div className={styles.metrics}>
        <StatCard label="Missões atribuídas" value={missions.length} variant="info" />
        <StatCard label="Em aberto" value={open.length} hint="Exclui concluídas" variant="warn" />
        <StatCard label="Concluídas" value={done.length} variant="success" />
      </div>

      {urgent ? (
        <SectionCard
          title="Missão mais urgente"
          variant="alert"
          hint="Ordenada por prioridade e pela última atualização."
        >
          <div className={styles.urgentCard}>
            <Link className={styles.urgentTitle} to={`/missoes/${urgent.id}`}>
              {urgent.titulo}
            </Link>
            <div className={styles.urgentMeta}>
              <PriorityBadge prioridade={urgent.prioridade} />
              <StatusBadge status={urgent.status} />
              <span className={styles.muted}>{urgent.tipoAmeaca.replaceAll("_", " ")}</span>
            </div>
          </div>
        </SectionCard>
      ) : (
        <SectionCard title="Missão mais urgente" hint="Sem missões em aberto no momento.">
          <p className={styles.muted}>Quando houver designações ativas, a mais prioritária aparecerá aqui.</p>
        </SectionCard>
      )}

      <SectionCard title="Próximos passos">
        <ul className={styles.hintList}>
          <li>Revise <Link to="/heroi/minhas-missoes">todas as missões</Link> e abra o dossiê para linha do tempo.</li>
          <li>Mantenha sua disponibilidade alinhada com a coordenação (atualização é feita no cadastro).</li>
        </ul>
      </SectionCard>
    </div>
  );
}
