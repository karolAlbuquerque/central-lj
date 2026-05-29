import { lazy, Suspense, useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { HeroAvailabilityBadge } from "../../components/HeroAvailabilityBadge/HeroAvailabilityBadge";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatCard } from "../../components/StatCard/StatCard";
import { api } from "../../services/api";
import type { HeroDetail } from "../../types/mission";
import type { Mission, MissionCombatState } from "../../types/pvp";
import styles from "./HeroAreaPage.module.css";

const MARTIAN_MODEL = "/martian_manhunter_lowpoly.glb";

const GltfHeroViewer = lazy(() =>
  import("../../components/GltfHeroViewer/GltfHeroViewer").then((m) => ({ default: m.GltfHeroViewer }))
);

const STATE_LABEL: Record<MissionCombatState, string> = {
  LOBBY: "Lobby",
  ACTIVE: "Ativa",
  NORMAL: "Normal",
  ALERTA_INFILTRACAO: "Alerta",
  EM_DUELO: "Em duelo",
  SABOTADA: "Sabotada",
  DEFENDIDA: "Defendida",
  SEM_CHEFE: "Sem chefe",
  EM_CRISE: "Em crise",
  COMPROMETIDA: "Comprometida"
};

const DONE: MissionCombatState = "COMPROMETIDA";

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
      const my = await api.listMissions();
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

  const open = missions.filter((m) => m.combatState !== DONE);
  const done = missions.filter((m) => m.combatState === DONE);
  const urgent = [...open].sort(
    (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
  )[0];
  const nomeHeroico = detail?.heroi.nomeHeroico;

  if (loading) {
    return <LoadingState message="Sincronizando sua área operacional…" />;
  }

  if (err) {
    return <p className={styles.error}>{err}</p>;
  }

  return (
    <div className={styles.page}>
      <div className={styles.heroGrid}>
        <div className={styles.heroContent}>
          <PageHeader
            kicker="Console do herói"
            title={nomeHeroico ? `${nomeHeroico}` : `Olá, ${user?.nome ?? "operador"}`}
            description="Acompanhe as missões em que você participa e coordene sua equipe."
            actions={
              <Link className={styles.btnPrimary} to="/missoes">
                Ver missões
              </Link>
            }
          />

          {detail ? (
            <div className={styles.profileStrip}>
              <div>
                <span className={styles.stripLabel}>Status</span>
                <HeroAvailabilityBadge status={detail.heroi.statusDisponibilidade} />
              </div>
              <div>
                <span className={styles.stripLabel}>Especialidade</span>
                <p className={styles.stripValue}>{detail.heroi.especialidade}</p>
              </div>
              <div>
                <span className={styles.stripLabel}>Nível</span>
                <p className={styles.stripValue}>{detail.heroi.nivel}</p>
              </div>
              {detail.heroi.equipe ? (
                <div>
                  <span className={styles.stripLabel}>Equipe</span>
                  <p className={styles.stripValue}>{detail.heroi.equipe.nome}</p>
                </div>
              ) : null}
              {detail.heroi.nomeCivil ? (
                <div>
                  <span className={styles.stripLabel}>Nome civil</span>
                  <p className={styles.stripValue}>{detail.heroi.nomeCivil}</p>
                </div>
              ) : null}
            </div>
          ) : null}

          <div className={styles.metrics}>
            <StatCard label="Missões ativas" value={open.length} variant="info" />
            <StatCard label="Concluídas" value={done.length} variant="success" />
          </div>

          {urgent ? (
            <SectionCard title="Prioridade imediata">
              <Link className={styles.urgentTitle} to={`/missoes/${urgent.id}`}>
                {urgent.titulo}
              </Link>
              <p className={styles.urgentMeta}>{STATE_LABEL[urgent.combatState]}</p>
            </SectionCard>
          ) : null}

          <SectionCard title="Ações rápidas">
            <div className={styles.quickActions}>
              <Link className={styles.btnPrimary} to="/missoes">
                Minhas missões
              </Link>
              <Link className={styles.btnGhost} to="/missoes/nova">
                Nova missão
              </Link>
            </div>
            <ul className={styles.tips}>
              <li>
                Crie uma <Link to="/missoes/nova">missão</Link>, convide aliados e fique alerta a
                infiltrações.
              </li>
              <li>
                Gerencie <Link to="/missoes">tarefas e equipe</Link> na sala de comando de cada
                missão.
              </li>
            </ul>
          </SectionCard>
        </div>

        <div className={styles.viewerCol}>
          <Suspense fallback={null}>
            <GltfHeroViewer
              modelUrl={MARTIAN_MODEL}
              height={540}
              normalizeDimensions
              normalizedTargetSize={2.0}
              cameraFov={48}
              cameraPosition={[0, 0.1, 8]}
              autoPlayAnimations
              enableMouseOrbit
            />
          </Suspense>
        </div>
      </div>
    </div>
  );
}
