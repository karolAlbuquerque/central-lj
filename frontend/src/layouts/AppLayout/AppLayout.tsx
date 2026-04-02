import type { ReactNode } from "react";
import { AppShell } from "../../components/AppShell/AppShell";
import {
  IconDashboard,
  IconHero,
  IconPlus,
  IconTarget,
  IconTeam
} from "../../components/NavIcons/NavIcons";

export function AppLayout({ children }: { children: ReactNode }) {
  return (
    <AppShell
      mode="admin"
      navItems={[
        { to: "/", label: "Painel", end: true, icon: <IconDashboard /> },
        { to: "/missoes", label: "Missões", icon: <IconTarget /> },
        { to: "/herois", label: "Heróis", icon: <IconHero /> },
        { to: "/equipes", label: "Equipes", icon: <IconTeam /> },
        { to: "/operacoes/nova", label: "Nova missão", icon: <IconPlus /> }
      ]}
    >
      {children}
    </AppShell>
  );
}
