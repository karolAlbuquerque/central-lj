import type { ReactNode } from "react";
import { NavLink } from "react-router-dom";
import styles from "./AppLayout.module.css";

export function AppLayout({ children }: { children: ReactNode }) {
  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <div className={styles.brandRow}>
          <div className={styles.brand}>
            <div className={styles.badge}>Central-LJ</div>
            <div className={styles.subtitle}>Central de Missões da Liga da Justiça</div>
          </div>
          <span className={styles.phase}>N2 • Operações + timeline</span>
        </div>
        <nav className={styles.nav}>
          <NavLink className={({ isActive }) => (isActive ? styles.navLinkActive : styles.navLink)} end to="/">
            Painel
          </NavLink>
          <NavLink
            className={({ isActive }) => (isActive ? styles.navLinkActive : styles.navLink)}
            to="/operacoes/nova"
          >
            Nova missão
          </NavLink>
        </nav>
      </header>
      <main className={styles.main}>{children}</main>
    </div>
  );
}
