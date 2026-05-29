import type { ReactNode } from "react";
import { AppShell } from "../../components/AppShell/AppShell";
import { IconTarget } from "../../components/NavIcons/NavIcons";

export function VillainLayout({ children }: { children: ReactNode }) {
  const items = [
    { to: "/vilao/ops", label: "Operações", icon: <IconTarget /> }
  ];

  return (
    <AppShell mode="villain" navItems={items}>
      {children}
    </AppShell>
  );
}
