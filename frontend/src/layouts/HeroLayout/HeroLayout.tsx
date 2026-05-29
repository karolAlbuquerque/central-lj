import type { ReactNode } from "react";
import { AppShell } from "../../components/AppShell/AppShell";
import { IconHero, IconPlus, IconTarget, IconUserCircle } from "../../components/NavIcons/NavIcons";
import { useAuth } from "../../auth/AuthContext";

export function HeroLayout({ children }: { children: ReactNode }) {
  const { user } = useAuth();

  const items = [
    { to: "/heroi/area", label: "Minha área", icon: <IconUserCircle /> },
    { to: "/missoes", label: "Missões", icon: <IconTarget /> },
    { to: "/missoes/nova", label: "Nova missão", icon: <IconPlus /> }
  ];
  if (user?.heroiId) {
    items.push({
      to: `/herois/${user.heroiId}`,
      label: "Perfil heroico",
      icon: <IconHero />
    });
  }

  return (
    <AppShell mode="hero" navItems={items}>
      {children}
    </AppShell>
  );
}
