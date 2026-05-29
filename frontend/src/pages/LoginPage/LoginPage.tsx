import { FormEvent, useEffect, useRef, useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { setAuthToken } from "../../services/api";
import styles from "./LoginPage.module.css";

export function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from;
  const search = new URLSearchParams(location.search);
  const reason = search.get("reason");
  const sessionExpired = reason === "session_expired";
  const nextAfterLogin = safeReturnPath(search.get("next")) ?? from ?? null;

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const emailRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (sessionExpired) {
      setAuthToken(null);
    }
  }, [sessionExpired]);

  useEffect(() => {
    const syncAutofill = () => {
      const domEmail = emailRef.current?.value?.trim() ?? "";
      const domPassword = passwordRef.current?.value ?? "";
      if (domEmail && domEmail !== email) setEmail(domEmail);
      if (domPassword && domPassword !== password) setPassword(domPassword);
    };
    syncAutofill();
    const t1 = window.setTimeout(syncAutofill, 100);
    const t2 = window.setTimeout(syncAutofill, 500);
    return () => {
      window.clearTimeout(t1);
      window.clearTimeout(t2);
    };
  }, [email, password]);

  if (user) {
    const dest =
      user.role === "VILLAIN"
        ? "/vilao/ops"
        : user.role === "HERO"
          ? "/heroi/area"
          : from && !from.startsWith("/login")
            ? from
            : "/";
    return <Navigate to={dest} replace />;
  }

  function fillDemo(demoEmail: string, demoPassword: string) {
    setEmail(demoEmail);
    setPassword(demoPassword);
    if (emailRef.current) emailRef.current.value = demoEmail;
    if (passwordRef.current) passwordRef.current.value = demoPassword;
    setError(null);
  }

  async function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const trimmedEmail = String(fd.get("email") ?? emailRef.current?.value ?? email).trim();
    const pwd = String(fd.get("password") ?? passwordRef.current?.value ?? password);
    if (!trimmedEmail) {
      setError("Informe seu e-mail.");
      return;
    }
    if (!pwd) {
      setError("Informe sua senha.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const loggedIn = await login(trimmedEmail, pwd);
      const dest =
        nextAfterLogin ??
        (loggedIn.role === "VILLAIN"
          ? "/vilao/ops"
          : loggedIn.role === "HERO"
            ? "/heroi/area"
            : "/");
      navigate(dest, { replace: true });
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
              <li>Papéis ADMIN / HERO / VILLAIN / OPERATOR</li>
            </ul>
          </div>
        </aside>

        <div className={styles.formSide}>
          <div className={styles.card}>
            <p className={styles.welcome}>Bem-vindo</p>
            <h1 className={styles.title}>Acesso à central</h1>
            <p className={styles.lead}>
              Entre com suas credenciais ou crie uma nova conta.
            </p>
            {sessionExpired ? (
              <div className={styles.infoBox} role="status">
                <strong>Sessão expirada.</strong>
                <span>Sua autenticação expirou. Faça login novamente para continuar.</span>
              </div>
            ) : null}
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
                  ref={emailRef}
                  id="email"
                  name="email"
                  className={styles.input}
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  onInput={(e) => setEmail(e.currentTarget.value)}
                  required
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label} htmlFor="password">
                  Senha
                </label>
                <input
                  ref={passwordRef}
                  id="password"
                  name="password"
                  className={styles.input}
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  onInput={(e) => setPassword(e.currentTarget.value)}
                  required
                />
              </div>
              <button className={styles.btn} type="submit" disabled={busy}>
                {busy ? "Autenticando…" : "Entrar na central"}
              </button>
            </form>

            <div className={styles.divider}><span>ou</span></div>

            <Link to="/cadastro" className={styles.btnRegister}>
              Criar nova conta
            </Link>

            <div className={styles.demoBox}>
              <p className={styles.demoTitle}>Contas de demonstração</p>
              <button
                type="button"
                className={styles.demoRow}
                onClick={() => fillDemo("coordenacao@central-lj.demo", "Admin@demo2026")}
              >
                <span className={styles.demoRole}>Coordenação</span>
                <code className={styles.demoEmail}>coordenacao@central-lj.demo</code>
                <code className={styles.demoPass}>Admin@demo2026</code>
              </button>
              <button
                type="button"
                className={styles.demoRow}
                onClick={() => fillDemo("heroi.demo@central-lj.demo", "Hero@demo2026")}
              >
                <span className={styles.demoRole}>Herói</span>
                <code className={styles.demoEmail}>heroi.demo@central-lj.demo</code>
                <code className={styles.demoPass}>Hero@demo2026</code>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function safeReturnPath(raw: string | null): string | null {
  if (!raw) return null;
  try {
    const path = decodeURIComponent(raw);
    if (path.startsWith("/") && !path.startsWith("//") && !path.startsWith("/login")) {
      return path;
    }
  } catch {
    return null;
  }
  return null;
}
