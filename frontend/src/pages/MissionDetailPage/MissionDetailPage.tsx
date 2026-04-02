import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { LoadingState } from "../../components/LoadingState/LoadingState";
import { PriorityBadge } from "../../components/PriorityBadge/PriorityBadge";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { StatusBadge } from "../../components/StatusBadge/StatusBadge";
import { Timeline } from "../../components/Timeline/Timeline";
import { useMissionUpdates } from "../../hooks/useMissionUpdates";
import { api } from "../../services/api";
import type { Equipe, Hero, MissionDetail } from "../../types/mission";
import styles from "./MissionDetailPage.module.css";

export function MissionDetailPage() {
  const { user } = useAuth();
  const canAssign = user?.role === "ADMIN" || user?.role === "OPERATOR";
  const backTo = user?.role === "HERO" ? "/heroi/minhas-missoes" : "/missoes";
  const backLabel = user?.role === "HERO" ? "← Minhas missões" : "← Lista de missões";

  const { id } = useParams<{ id: string }>();
  const [detail, setDetail] = useState<MissionDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [heroes, setHeroes] = useState<Hero[]>([]);
  const [teams, setTeams] = useState<Equipe[]>([]);
  const [selHero, setSelHero] = useState("");
  const [selTeam, setSelTeam] = useState("");
  const [por, setPor] = useState("Coordenação");
  const [assignBusy, setAssignBusy] = useState(false);
  const [assignErr, setAssignErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!id) return;
    try {
      const d = await api.getMissionDetail(id);
      setDetail(d);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao carregar.");
    }
  }, [id]);

  useEffect(() => {
    if (!canAssign) {
      return;
    }
    void api.listHeroes().then(setHeroes).catch(() => {});
    void api.listTeams().then(setTeams).catch(() => {});
  }, [canAssign]);

  useMissionUpdates(load, 12000);

  async function assignHero() {
    if (!id || !selHero) return;
    setAssignBusy(true);
    setAssignErr(null);
    try {
      await api.assignMissionToHero(id, selHero, por.trim() || null);
      await load();
    } catch (e) {
      setAssignErr(e instanceof Error ? e.message : "Falha na atribuição.");
    } finally {
      setAssignBusy(false);
    }
  }

  async function assignTeam() {
    if (!id || !selTeam) return;
    setAssignBusy(true);
    setAssignErr(null);
    try {
      await api.assignMissionToTeam(id, selTeam, por.trim() || null);
      await load();
    } catch (e) {
      setAssignErr(e instanceof Error ? e.message : "Falha na atribuição.");
    } finally {
      setAssignBusy(false);
    }
  }

  if (!id) {
    return <p className={styles.error}>ID inválido.</p>;
  }

  if (error) {
    return (
      <div className={styles.page}>
        <Link className={styles.back} to={backTo}>
          {backLabel}
        </Link>
        <p className={styles.error}>{error}</p>
      </div>
    );
  }

  if (!detail) {
    return (
      <div className={styles.page}>
        <Link className={styles.back} to={backTo}>
          {backLabel}
        </Link>
        <LoadingState message="Carregando dossiê da missão…" />
      </div>
    );
  }

  const { missao: m, historico } = detail;
  const ameacaFmt = m.tipoAmeaca.replaceAll("_", " ");

  return (
    <div className={styles.page}>
      <Link className={styles.back} to={backTo}>
        {backLabel}
      </Link>

      <div className={styles.grid}>
        <section className={`${styles.panel} ${styles.panelHero}`}>
          <div className={styles.titleRow}>
            <h1 className={styles.title}>{m.titulo}</h1>
            <div className={styles.badges}>
              <StatusBadge status={m.status} />
              <PriorityBadge prioridade={m.prioridade} />
              <span className={styles.threatPill} title="Tipo de ameaça">
                {ameacaFmt}
              </span>
            </div>
          </div>
          <div className={styles.meta}>
            <div>
              <strong>Criada</strong> {new Date(m.dataCriacao).toLocaleString()}
            </div>
            <div>
              <strong>Última atualização</strong> {new Date(m.ultimaAtualizacao).toLocaleString()}
            </div>
            {m.localizacao &&
              (m.localizacao.cidade || m.localizacao.bairro || m.localizacao.referencia) && (
                <div>
                  <strong>Local</strong>{" "}
                  {[m.localizacao.cidade, m.localizacao.bairro, m.localizacao.referencia]
                    .filter(Boolean)
                    .join(" · ")}
                </div>
              )}
          </div>
          {m.descricao ? <p className={styles.desc}>{m.descricao}</p> : null}
          <p className={styles.mono}>id: {m.id}</p>
        </section>

        <div className={styles.lowerGrid}>
        <SectionCard
          title="Designação e atribuição"
          hint={
            canAssign ? (
              <>
                Designe um <strong>herói</strong> ou uma <strong>equipe</strong>. A designação é exclusiva neste MVP
                e substitui a anterior.
              </>
            ) : (
              "Designação registrada pela coordenação. Alterações no painel administrativo."
            )
          }
        >
          {m.atribuicao && (m.atribuicao.nomeHeroico || m.atribuicao.nomeEquipe) ? (
            <div className={styles.assignCurrent}>
              <div>
                <strong>Responsável:</strong>{" "}
                {m.atribuicao.nomeHeroico ? (
                  m.atribuicao.heroiId ? (
                    <Link className={styles.heroLink} to={`/herois/${m.atribuicao.heroiId}`}>
                      {m.atribuicao.nomeHeroico}
                    </Link>
                  ) : (
                    m.atribuicao.nomeHeroico
                  )
                ) : null}
                {m.atribuicao.nomeEquipe ? (
                  <>
                    {m.atribuicao.equipeId ? (
                      canAssign ? (
                        <Link className={styles.heroLink} to={`/equipes/${m.atribuicao.equipeId}`}>
                          {m.atribuicao.nomeEquipe}
                        </Link>
                      ) : (
                        m.atribuicao.nomeEquipe
                      )
                    ) : (
                      m.atribuicao.nomeEquipe
                    )}{" "}
                    <span className={styles.inlineHint}>(equipe)</span>
                  </>
                ) : null}
              </div>
              {m.atribuicao.atribuidoEm ? (
                <div className={styles.inlineHint}>
                  Atribuído em {new Date(m.atribuicao.atribuidoEm).toLocaleString()}
                  {m.atribuicao.atribuidoPor ? ` por ${m.atribuicao.atribuidoPor}` : null}
                </div>
              ) : null}
            </div>
          ) : (
            <p className={styles.inlineHint}>Nenhuma designação registrada ainda.</p>
          )}
          {canAssign ? (
            <>
              <div className={styles.assignRows}>
                <div className={styles.assignRow}>
                  <label className={styles.assignLbl} htmlFor="assign-hero">
                    Herói
                  </label>
                  <select
                    id="assign-hero"
                    className={styles.select}
                    value={selHero}
                    onChange={(e) => setSelHero(e.target.value)}
                  >
                    <option value="">— Selecionar —</option>
                    {heroes
                      .filter((h) => h.ativo)
                      .map((h) => (
                        <option key={h.id} value={h.id}>
                          {h.nomeHeroico} ({h.especialidade})
                        </option>
                      ))}
                  </select>
                  <button
                    type="button"
                    className={styles.btnAssign}
                    disabled={assignBusy || !selHero}
                    onClick={() => void assignHero()}
                  >
                    Designar herói
                  </button>
                </div>
                <div className={styles.assignRow}>
                  <label className={styles.assignLbl} htmlFor="assign-team">
                    Equipe
                  </label>
                  <select
                    id="assign-team"
                    className={styles.select}
                    value={selTeam}
                    onChange={(e) => setSelTeam(e.target.value)}
                  >
                    <option value="">— Selecionar —</option>
                    {teams
                      .filter((t) => t.ativa)
                      .map((t) => (
                        <option key={t.id} value={t.id}>
                          {t.nome}
                        </option>
                      ))}
                  </select>
                  <button
                    type="button"
                    className={styles.btnAssign}
                    disabled={assignBusy || !selTeam}
                    onClick={() => void assignTeam()}
                  >
                    Designar equipe
                  </button>
                </div>
                <div className={styles.assignRow}>
                  <label className={styles.assignLbl} htmlFor="assign-by">
                    Registrado por (opcional)
                  </label>
                  <input
                    id="assign-by"
                    className={styles.input}
                    value={por}
                    onChange={(e) => setPor(e.target.value)}
                    placeholder="Coordenação"
                  />
                </div>
              </div>
              {assignErr ? <p className={styles.error}>{assignErr}</p> : null}
            </>
          ) : null}
        </SectionCard>

        <SectionCard
          title="Linha do tempo"
          hint="Histórico persistido a cada transição (API e pipeline Kafka). Atualização em tempo quase real via SSE + polling."
        >
          <Timeline entries={historico} />
        </SectionCard>
        </div>
      </div>
    </div>
  );
}
