import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { EmptyState } from "../../components/EmptyState/EmptyState";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { SectionCard } from "../../components/SectionCard/SectionCard";
import { api } from "../../services/api";
import type { SabotageType } from "../../types/pvp";
import styles from "./SabotagePage.module.css";

type SabotageOption = { type: SabotageType; label: string; description: string };

const OPTIONS: SabotageOption[] = [
  {
    type: "BLOCK_TASK",
    label: "Bloquear tarefa",
    description: "Impede o progresso de uma tarefa crítica da missão por tempo limitado."
  },
  {
    type: "SLOW_PROGRESS",
    label: "Atrasar progresso",
    description: "Reduz a velocidade de conclusão das tarefas do herói por 10 minutos."
  },
  {
    type: "REMOVE_MEMBER",
    label: "Expulsar membro",
    description: "Remove temporariamente um membro aleatório da equipe inimiga."
  },
  {
    type: "SILENT_OBSERVE",
    label: "Observação silenciosa",
    description: "Mantém acesso às informações da missão sem deixar rastros imediatos."
  }
];

export function SabotagePage() {
  const { duelId } = useParams<{ duelId: string }>();
  const navigate = useNavigate();
  const [selected, setSelected] = useState<SabotageType | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  if (!duelId) return <EmptyState title="Duelo não encontrado" />;

  const handleConfirm = async () => {
    if (!selected) return;
    setSubmitting(true);
    setErr(null);
    try {
      await api.submitSabotageChoice(duelId, selected, "EQUIPE");
      setDone(true);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao aplicar sabotagem.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Vitória"
        title="Escolha sua sabotagem"
        description="Você venceu o duelo. Selecione o tipo de sabotagem a aplicar na missão inimiga."
      />

      {err ? <p className={styles.error}>{err}</p> : null}

      {done ? (
        <SectionCard title="Sabotagem aplicada">
          <p className={styles.successMsg}>Sabotagem executada com sucesso.</p>
          <button
            type="button"
            className={styles.btnConfirm}
            onClick={() => navigate("/vilao/ops")}
          >
            Voltar às operações
          </button>
        </SectionCard>
      ) : (
        <SectionCard title="Tipo de sabotagem">
          <div className={styles.optionList}>
            {OPTIONS.map(opt => (
              <div
                key={opt.type}
                className={`${styles.optionCard} ${selected === opt.type ? styles.selected : ""}`}
                onClick={() => setSelected(opt.type)}
              >
                <p className={styles.optionTitle}>{opt.label}</p>
                <p className={styles.optionDesc}>{opt.description}</p>
              </div>
            ))}
          </div>
          <button
            type="button"
            className={styles.btnConfirm}
            disabled={!selected || submitting}
            onClick={() => { void handleConfirm(); }}
          >
            {submitting ? "Aplicando…" : "Confirmar sabotagem"}
          </button>
        </SectionCard>
      )}
    </div>
  );
}
