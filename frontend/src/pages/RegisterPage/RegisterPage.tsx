import { FormEvent, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { api } from "../../services/api";
import styles from "./RegisterPage.module.css";

type Role = "HERO" | "VILLAIN";

const ROLES: { value: Role; icon: string; label: string; desc: string }[] = [
  { value: "HERO", icon: "⚡", label: "Herói", desc: "Crie e gerencie missões" },
  { value: "VILLAIN", icon: "💀", label: "Vilão", desc: "Infiltre e sabote missões" }
];

export function RegisterPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();

  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<Role>("HERO");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (user) {
    const dest = user.role === "VILLAIN" ? "/vilao/ops" : user.role === "HERO" ? "/heroi/area" : "/";
    return <Navigate to={dest} replace />;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    const trimmedNome = nome.trim();
    const trimmedEmail = email.trim();
    if (!trimmedNome) {
      setError("Informe seu nome.");
      return;
    }
    if (!trimmedEmail) {
      setError("Informe seu e-mail.");
      return;
    }
    if (!password) {
      setError("Informe sua senha.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await api.register({ nome: trimmedNome, email: trimmedEmail, password, role });
      const loggedIn = await login(trimmedEmail, password);
      const dest =
        loggedIn.role === "VILLAIN" ? "/vilao/ops" : loggedIn.role === "HERO" ? "/heroi/area" : "/";
      navigate(dest, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível cadastrar.");
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
            <h2 className={styles.heroTitle}>Junte-se à Central de Missões</h2>
            <p className={styles.heroLead}>
              Crie sua conta como herói — construa missões e gerencie sua equipe — ou como vilão,
              para infiltrar e desafiar os heróis em duelos de puzzles.
            </p>
          </div>
        </aside>

        <div className={styles.formSide}>
          <div className={styles.card}>
            <p className={styles.welcome}>Cadastro</p>
            <h1 className={styles.title}>Criar conta</h1>
            <p className={styles.lead}>Escolha seu papel e preencha seus dados.</p>

            <form onSubmit={(e) => void onSubmit(e)} className={styles.form} noValidate>
              {error ? (
                <div className={styles.errorBox} role="alert">
                  <strong>Erro ao cadastrar.</strong>
                  <span>{error}</span>
                </div>
              ) : null}

              <div className={styles.roleRow}>
                {ROLES.map((r) => (
                  <button
                    key={r.value}
                    type="button"
                    className={`${styles.roleCard} ${
                      role === r.value
                        ? r.value === "VILLAIN"
                          ? styles.activeVillain
                          : styles.active
                        : ""
                    }`}
                    onClick={() => setRole(r.value)}
                  >
                    <span className={styles.roleIcon}>{r.icon}</span>
                    <span className={styles.roleLabel}>{r.label}</span>
                    <span className={styles.roleDesc}>{r.desc}</span>
                  </button>
                ))}
              </div>

              <div className={styles.field}>
                <label className={styles.label} htmlFor="nome">Nome</label>
                <input
                  id="nome"
                  className={styles.input}
                  type="text"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                  minLength={2}
                  maxLength={100}
                />
              </div>

              <div className={styles.field}>
                <label className={styles.label} htmlFor="email">E-mail</label>
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
                <label className={styles.label} htmlFor="password">Senha</label>
                <input
                  id="password"
                  className={styles.input}
                  type="password"
                  autoComplete="new-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  minLength={6}
                />
              </div>

              <button className={styles.btn} type="submit" disabled={busy}>
                {busy ? "Cadastrando…" : "Criar conta"}
              </button>
            </form>

            <p className={styles.loginLink}>
              Já tem conta? <Link to="/login">Entrar na central</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
