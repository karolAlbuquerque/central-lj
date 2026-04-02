import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { PageHeader } from "../../components/PageHeader/PageHeader";
import { api } from "../../services/api";
import type { CreateMissionPayload, PrioridadeMissao, TipoAmeaca } from "../../types/mission";
import styles from "./NewMissionPage.module.css";

const TIPOS: TipoAmeaca[] = [
  "INVASAO",
  "DESASTRE_NATURAL",
  "META_HUMANO",
  "TECNOLOGICA",
  "DESCONHECIDA"
];
const PRIOS: PrioridadeMissao[] = ["BAIXA", "MEDIA", "ALTA", "CRITICA"];

export function NewMissionPage() {
  const navigate = useNavigate();
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [titulo, setTitulo] = useState("");
  const [descricao, setDescricao] = useState("");
  const [tipoAmeaca, setTipoAmeaca] = useState<TipoAmeaca>("TECNOLOGICA");
  const [prioridade, setPrioridade] = useState<PrioridadeMissao>("MEDIA");
  const [cidade, setCidade] = useState("");
  const [bairro, setBairro] = useState("");
  const [referencia, setReferencia] = useState("");

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setErr(null);
    if (!titulo.trim()) {
      setErr("Informe o título.");
      return;
    }
    const payload: CreateMissionPayload = {
      titulo: titulo.trim(),
      descricao: descricao.trim() || null,
      tipoAmeaca,
      prioridade,
      cidade: cidade.trim() || null,
      bairro: bairro.trim() || null,
      referencia: referencia.trim() || null
    };
    setBusy(true);
    try {
      const created = await api.createMission(payload);
      navigate(`/missoes/${created.id}`);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Falha ao criar.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={styles.page}>
      <PageHeader
        kicker="Registro operacional"
        title="Nova missão"
        description={
          <>
            A ocorrência nasce como <strong>RECEBIDA</strong>, persiste no PostgreSQL e publica em{" "}
            <code>missions.created</code>. O consumer evolui o status; acompanhe no detalhe ou no painel.
          </>
        }
        actions={
          <Link className={styles.back} to="/">
            ← Painel
          </Link>
        }
      />
      <div className={styles.card}>
        <form className={styles.form} onSubmit={(e) => void onSubmit(e)}>
          <label className={styles.field}>
            <span>Título</span>
            <input
              className={styles.input}
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              autoComplete="off"
            />
          </label>
          <label className={styles.field}>
            <span>Descrição</span>
            <textarea
              className={styles.textarea}
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              rows={4}
            />
          </label>
          <label className={styles.field}>
            <span>Tipo de ameaça</span>
            <select
              className={styles.select}
              value={tipoAmeaca}
              onChange={(e) => setTipoAmeaca(e.target.value as TipoAmeaca)}
            >
              {TIPOS.map((t) => (
                <option key={t} value={t}>
                  {t.replaceAll("_", " ")}
                </option>
              ))}
            </select>
          </label>
          <label className={styles.field}>
            <span>Prioridade</span>
            <select
              className={styles.select}
              value={prioridade}
              onChange={(e) => setPrioridade(e.target.value as PrioridadeMissao)}
            >
              {PRIOS.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </label>
          <p className={styles.sub}>Localização (opcional)</p>
          <label className={styles.field}>
            <span>Cidade</span>
            <input className={styles.input} value={cidade} onChange={(e) => setCidade(e.target.value)} />
          </label>
          <label className={styles.field}>
            <span>Bairro</span>
            <input className={styles.input} value={bairro} onChange={(e) => setBairro(e.target.value)} />
          </label>
          <label className={styles.field}>
            <span>Referência</span>
            <input
              className={styles.input}
              value={referencia}
              onChange={(e) => setReferencia(e.target.value)}
            />
          </label>
          {err && <p className={styles.error}>{err}</p>}
          <button type="submit" className={styles.btn} disabled={busy}>
            {busy ? "Enviando…" : "Registrar e abrir detalhe"}
          </button>
        </form>
      </div>
    </div>
  );
}
