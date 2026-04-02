import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { api } from "../../services/api";
import type { Equipe } from "../../types/mission";
import styles from "./TeamsListPage.module.css";

export function TeamsListPage() {
  const [teams, setTeams] = useState<Equipe[]>([]);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setTeams(await api.listTeams());
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
        kicker="Unidades"
        title="Equipes heroicas"
        description="Grupos táticos cadastrados na central. Equipes ativas podem ser designadas em missões."
        actions={
          <Link className={styles.btnPrimary} to="/equipes/nova">
            + Nova equipe
          </Link>
        }
      />

      {err ? <p className={styles.error}>{err}</p> : null}

      {teams.length === 0 && !err ? (
        <EmptyState
          title="Nenhuma equipe"
          hint="Crie uma equipe para agrupar heróis em operações conjuntas."
          action={
            <Link className={styles.btnGhost} to="/equipes/nova">
              Cadastrar equipe
            </Link>
          }
        />
      ) : null}

      {teams.length > 0 ? (
        <div className={styles.grid}>
          {teams.map((t) => (
            <article key={t.id} className={styles.card}>
              <div className={styles.cardHeader}>
                <Link className={styles.teamName} to={`/equipes/${t.id}`}>
                  {t.nome}
                </Link>
                <span className={t.ativa ? styles.badgeOn : styles.badgeOff}>{t.ativa ? "Ativa" : "Inativa"}</span>
              </div>
              <p className={styles.spec}>{t.especialidadePrincipal ?? "Especialidade não informada"}</p>
              <Link className={styles.cardCta} to={`/equipes/${t.id}`}>
                Ver composição →
              </Link>
            </article>
          ))}
        </div>
      ) : null}
    </div>
  );
}
