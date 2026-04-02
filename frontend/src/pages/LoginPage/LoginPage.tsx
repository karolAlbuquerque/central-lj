import { FormEvent, useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import styles from "./LoginPage.module.css";

export function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from;

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (user) {
    const dest =
      user.role === "HERO"
        ? "/heroi/area"
        : from && !from.startsWith("/login")
          ? from
          : "/";
    return <Navigate to={dest} replace />;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(email, password);
      navigate("/", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível entrar.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.split}>
        <aside className={styles.hero} aria-hidden>
          <div className={styles.heroInner}>
            <p className={styles.heroMark}>Central-LJ</p>
            <h2 className={styles.heroTitle}>Central de Missões da Liga da Justiça</h2>
            <p className={styles.heroLead}>
              Painel de comando para coordenação e console operacional para heróis — seguro, auditável e
              integrado ao pipeline de missões.
            </p>
            <ul className={styles.heroList}>
              <li>Monitoramento em tempo quase real</li>
              <li>Histórico Kafka e API unificado</li>
              <li>Papéis ADMIN / HERO / OPERATOR</li>
            </ul>
          </div>
        </aside>

        <div className={styles.formSide}>
          <div className={styles.card}>
            <p className={styles.welcome}>Bem-vindo</p>
            <h1 className={styles.title}>Acesso à central</h1>
            <p className={styles.lead}>
              Entre com credenciais de <strong>coordenação</strong> ou de <strong>herói</strong> (ambiente de
              demonstração).
            </p>
            <form onSubmit={(e) => void onSubmit(e)} className={styles.form} noValidate>
              {error ? (
                <div className={styles.errorBox} role="alert">
                  <strong>Não foi possível autenticar.</strong>
                  <span>{error}</span>
                </div>
              ) : null}
              <div className={styles.field}>
                <label className={styles.label} htmlFor="email">
                  E-mail
                </label>
                <input
                  id="email"
                  className={styles.input}
                  type="email"
                  autoComplete="username"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label} htmlFor="password">
                  Senha
                </label>
                <input
                  id="password"
                  className={styles.input}
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              <button className={styles.btn} type="submit" disabled={busy}>
                {busy ? "Autenticando…" : "Entrar na central"}
              </button>
            </form>
            <p className={styles.hint}>
              Credenciais de demo: <code>README.md</code> e{" "}
              <code>docs/n2/09-autenticacao-e-papeis.md</code>.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
