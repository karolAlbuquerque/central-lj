import type { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import styles from "./AppShell.module.css";

export type AppShellNavItem = {
  to: string;
  label: string;
  end?: boolean;
  icon?: ReactNode;
};

type Props = {
  mode: "admin" | "hero";
  navItems: AppShellNavItem[];
  children: ReactNode;
};

function initials(nome: string | undefined): string {
  if (!nome?.trim()) return "?";
  const p = nome.trim().split(/\s+/).slice(0, 2);
  return p.map((s) => s[0]?.toUpperCase() ?? "").join("") || "?";
}

const ADMIN_HEAD = {
  title: "Coordenação tática",
  sub: "Central de Missões da Liga da Justiça — comando, monitoramento e designação."
};

const HERO_HEAD = {
  title: "Operação ativa",
  sub: "Console do herói: missões designadas, prioridades e linha do tempo."
};

export function AppShell({ mode, navItems, children }: Props) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const shellClass = mode === "admin" ? styles.shellAdmin : styles.shellHero;
  const head = mode === "admin" ? ADMIN_HEAD : HERO_HEAD;

  return (
    <div className={`${styles.shell} ${shellClass}`}>
      <aside className={styles.sidebar} aria-label="Navegação principal">
        <div className={styles.brandBlock}>
          <p className={styles.mark}>
            <span className={styles.markIcon} aria-hidden>
              ⚡
            </span>
            Central-LJ
          </p>
          <p className={styles.tagline}>
            {mode === "admin" ? "Comando · Coordenação" : "Console operacional"}
          </p>
        </div>
        <nav className={styles.nav}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? `${styles.navLink} ${styles.navLinkActive}` : styles.navLink)}
            >
              {item.icon ? <span className={styles.navIcon}>{item.icon}</span> : null}
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className={styles.userBlock}>
          <div className={styles.userLabel}>Sessão</div>
          <div className={styles.userRow}>
            <div className={styles.avatar} aria-hidden title={user?.nome ?? ""}>
              {initials(user?.nome)}
            </div>
            <div className={styles.userMeta}>
              <div className={styles.userName} title={user?.nome}>
                {user?.nome ?? "—"}
              </div>
              <div className={styles.userEmail} title={user?.email}>
                {user?.email}
              </div>
            </div>
          </div>
          <button
            type="button"
            className={styles.btnOut}
            onClick={() => {
              void logout();
              navigate("/login", { replace: true });
            }}
          >
            Encerrar sessão
          </button>
        </div>
      </aside>
      <div className={styles.main}>
        <header className={styles.topbar}>
          <div className={styles.topbarLeft}>
            <div className={styles.pill}>
              <span className={styles.pillDot} aria-hidden />
              {mode === "admin" ? "Central de operações" : "Área do herói"}
            </div>
            <h1 className={styles.topbarTitle}>{head.title}</h1>
            <p className={styles.topbarSub}>{head.sub}</p>
          </div>
          <div className={styles.topbarRight}>
            <span className={styles.phaseChip}>N2 · painel</span>
          </div>
        </header>
        <main className={styles.content}>
          <div className={styles.contentInner}>{children}</div>
        </main>
      </div>
    </div>
  );
}
