import { useState, type FormEvent } from "react";
import { api } from "../../services/api";
import type { MissionTestPayload } from "../../types/mission";
import styles from "./N1DevTools.module.css";

type PanelResult = { label: string; ok: boolean; data?: unknown; error?: string };

export function N1DevTools() {
  const [panel, setPanel] = useState<PanelResult | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  const [tituloTest, setTituloTest] = useState("");
  const [descricaoTest, setDescricaoTest] = useState("");
  const [kafkaMensagem, setKafkaMensagem] = useState("");

  async function run(label: string, fn: () => Promise<unknown>) {
    setBusy(label);
    setPanel(null);
    try {
      const data = await fn();
      setPanel({ label, ok: true, data });
    } catch (e) {
      setPanel({
        label,
        ok: false,
        error: e instanceof Error ? e.message : "Erro"
      });
    } finally {
      setBusy(null);
    }
  }

  function handleMissionTestSubmit(e: FormEvent) {
    e.preventDefault();
    const payload: MissionTestPayload = {
      titulo: tituloTest.trim(),
      descricao: descricaoTest.trim() || undefined
    };
    if (!payload.titulo) {
      setPanel({ label: "POST /api/missions/test", ok: false, error: "Informe o título." });
      return;
    }
    void run("POST /api/missions/test", () => api.postMissionTest(payload));
  }

  return (
    <details className={styles.wrap}>
      <summary>Ferramentas N1 (hello, missão teste HTTP, Kafka manual)</summary>
      <div className={styles.inner}>
        <p className={styles.note}>
          Rotas legadas para demonstração de arquitetura; o fluxo N2 usa <code>POST /api/missions</code>.
        </p>
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.btn}
            disabled={busy !== null}
            onClick={() => void run("GET /api/hello", () => api.getHello())}
          >
            GET /api/hello
          </button>
        </div>
        <form className={styles.form} onSubmit={handleMissionTestSubmit}>
          <span className={styles.formTitle}>Missão de teste (sem persistência N2 completa)</span>
          <input
            className={styles.input}
            value={tituloTest}
            onChange={(e) => setTituloTest(e.target.value)}
            placeholder="Título"
          />
          <textarea
            className={styles.textarea}
            value={descricaoTest}
            onChange={(e) => setDescricaoTest(e.target.value)}
            rows={2}
          />
          <button type="submit" className={styles.btnSec} disabled={busy !== null}>
            POST /api/missions/test
          </button>
        </form>
        <div className={styles.kafka}>
          <span className={styles.formTitle}>Tópico missions.events</span>
          <input
            className={styles.input}
            value={kafkaMensagem}
            onChange={(e) => setKafkaMensagem(e.target.value)}
            placeholder="Mensagem opcional"
          />
          <button
            type="button"
            className={styles.btnSec}
            disabled={busy !== null}
            onClick={() =>
              void run("POST /api/events/publish-test", () =>
                api.postPublishTestEvent(kafkaMensagem.trim() || undefined)
              )
            }
          >
            Publicar
          </button>
        </div>
        {panel && (
          <div className={panel.ok ? styles.ok : styles.err}>
            <strong>{panel.label}</strong>
            {panel.ok ? (
              <pre className={styles.pre}>{JSON.stringify(panel.data, null, 2)}</pre>
            ) : (
              <span>{panel.error}</span>
            )}
          </div>
        )}
      </div>
    </details>
  );
}
