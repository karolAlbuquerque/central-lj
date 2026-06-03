import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatCard } from "../../components/StatCard/StatCard";
import { useMissionUpdates } from "../../hooks/useMissionUpdates";
import { api } from "../../services/api";
import type { DuelSession, Mission } from "../../types/pvp";
import styles from "./VillainOpsPage.module.css";

const COMBAT_LABEL: Record<Mission["combatState"], string> = {
  LOBBY: "Em formação",
  ACTIVE: "Ativa",
  NORMAL: "Normal",
  ALERTA_INFILTRACAO: "Em alerta",
  EM_DUELO: "Em duelo",
  SABOTADA: "Sabotada",
  DERROTADA: "Derrotada",
  DEFENDIDA: "Defendida",
  SEM_CHEFE: "Sem chefe",
  EM_CRISE: "Em crise",
  COMPROMETIDA: "Comprometida"
};

export function VillainOpsPage() {
  const navigate = useNavigate();
  const [targets, setTargets] = useState<Mission[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [infiltrating, setInfiltrating] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setErr(null);
    try {
      setTargets(await api.listVillainTargets());
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar alvos.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  useMissionUpdates(load, 8000);

  const handleInfiltrate = async (missionId: string) => {
    setInfiltrating(missionId);
    try {
      const duel = await api.startInfiltration(missionId);
      navigate(`/vilao/duelo/${duel.id}`);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao iniciar infiltração.");
      setInfiltrating(null);
    }
  };

  if (loading) return <LoadingState message="Escaneando missões ativas…" />;

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Sala de guerra"
        title="Operações de infiltração"
        description="Selecione uma missão ativa. Após 3 puzzles de brecha, o combate 3×3 com o herói começa na hora — quem resolver 3 puzzles primeiro vence."
      />

      <div className={styles.metrics}>
        <StatCard label="Alvos disponíveis" value={targets.length} variant="info" />
        <StatCard
          label="Em alerta/duelo"
          value={targets.filter(t => !["NORMAL", "ACTIVE"].includes(t.combatState)).length}
          variant="warn"
        />
      </div>

      {err ? <p className={styles.error}>{err}</p> : null}

      {targets.length === 0 ? (
        <EmptyState title="Nenhum alvo disponível" hint="Não há missões ativas no momento." />
      ) : (
        <SectionCard title="Missões infiltráveis">
          <div className={styles.targetList}>
            {targets.map(t => (
              <div key={t.id} className={styles.targetCard}>
                <div className={styles.targetInfo}>
                  <span className={styles.targetTitle}>{t.titulo}</span>
                  <span className={styles.targetState}>{COMBAT_LABEL[t.combatState]}</span>
                </div>
                {t.descricao ? <p className={styles.targetDesc}>{t.descricao}</p> : null}
                <button
                  type="button"
                  className={styles.btnInfiltrate}
                  disabled={infiltrating === t.id}
                  onClick={() => { void handleInfiltrate(t.id); }}
                >
                  {infiltrating === t.id ? "Iniciando brecha…" : "Iniciar infiltração"}
                </button>
              </div>
            ))}
          </div>
        </SectionCard>
      )}
    </div>
  );
}
