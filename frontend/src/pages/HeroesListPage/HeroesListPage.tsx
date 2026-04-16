import { lazy, Suspense, useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { HeroAvailabilityBadge } from "../../components/HeroAvailabilityBadge/HeroAvailabilityBadge";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { HERO_GLTF_VIEW_PRESET } from "../../components/GltfHeroViewer/heroViewPreset";
import { api } from "../../services/api";
import type { Hero } from "../../types/mission";
import styles from "./HeroesListPage.module.css";

const GltfHeroViewer = lazy(() =>
  import("../../components/GltfHeroViewer/GltfHeroViewer").then((m) => ({ default: m.GltfHeroViewer }))
);

const AQUAMAN_MODEL = "/fortnite_aquaman__chapter_2_season_3_bp_skin.glb";

export function HeroesListPage() {
  const [heroes, setHeroes] = useState<Hero[]>([]);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setHeroes(await api.listHeroes());
      setErr(null);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao carregar.");
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Elenco tático"
        title="Heróis"
        description="Ativos registrados na Central-LJ com disponibilidade, especialidade e vínculo de equipe — recurso central do produto."
        actions={
          <Link className={styles.btnPrimary} to="/herois/nova">
            + Cadastrar herói
          </Link>
        }
      />

      {err ? <p className={styles.error}>{err}</p> : null}

      <div className={styles.shell}>
        <div className={styles.mainColumn}>
          {heroes.length === 0 && !err ? (
            <EmptyState
              title="Elenco vazio"
              hint="Cadastre o primeiro herói para habilitar designações em missões."
              action={
                <Link className={styles.btnGhost} to="/herois/nova">
                  Novo herói
                </Link>
              }
            />
          ) : null}

          {heroes.length > 0 ? (
            <div className={styles.grid}>
              {heroes.map((h) => (
                <article key={h.id} className={`${styles.card} ${!h.ativo ? styles.cardInactive : ""}`}>
                  <div className={styles.cardTop}>
                    <Link className={styles.heroName} to={`/herois/${h.id}`}>
                      {h.nomeHeroico}
                    </Link>
                    {!h.ativo ? <span className={styles.inactiveTag}>Inativo</span> : null}
                  </div>
                  <p className={styles.spec}>{h.especialidade}</p>
                  <div className={styles.cardRow}>
                    <span className={styles.rowLabel}>Disponibilidade</span>
                    <HeroAvailabilityBadge status={h.statusDisponibilidade} />
                  </div>
                  <div className={styles.cardRow}>
                    <span className={styles.rowLabel}>Equipe</span>
                    <span className={styles.rowValue}>
                      {h.equipe ? (
                        <Link className={styles.inlineLink} to={`/equipes/${h.equipe.id}`}>
                          {h.equipe.nome}
                        </Link>
                      ) : (
                        "—"
                      )}
                    </span>
                  </div>
                  <Link className={styles.cardCta} to={`/herois/${h.id}`}>
                    Abrir ficha →
                  </Link>
                </article>
              ))}
            </div>
          ) : null}
        </div>
        <aside className={styles.heroColumn} aria-label="Aquaman — referência tática">
          <Suspense fallback={<div className={styles.heroFallback}>Carregando modelo 3D…</div>}>
            <GltfHeroViewer
              modelUrl={AQUAMAN_MODEL}
              {...HERO_GLTF_VIEW_PRESET}
              /* Aquaman + tridente: sobe o modelo e abre um pouco o quadro para não cortar os pés */
              groupPosition={[0, -0.18, 0]}
              cameraPosition={[0, 0.1, 7.62]}
            />
          </Suspense>
        </aside>
      </div>
    </div>
  );
}
