import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth/AuthContext";
import { RequireAuth } from "./components/RequireAuth/RequireAuth";
import { AppLayout } from "./layouts/AppLayout/AppLayout";
import { HeroLayout } from "./layouts/HeroLayout/HeroLayout";
import { VillainLayout } from "./layouts/VillainLayout/VillainLayout";
import { DashboardPage } from "./pages/DashboardPage/DashboardPage";
import { DuelArenaPage } from "./pages/DuelArenaPage/DuelArenaPage";
import { HeroAreaPage } from "./pages/HeroAreaPage/HeroAreaPage";
import { HeroDetailPage } from "./pages/HeroDetailPage/HeroDetailPage";
import { HeroesListPage } from "./pages/HeroesListPage/HeroesListPage";
import { LoginPage } from "./pages/LoginPage/LoginPage";
import { RegisterPage } from "./pages/RegisterPage/RegisterPage";
import { MissionCommandPage } from "./pages/MissionCommandPage/MissionCommandPage";
import { MissionTasksPage } from "./pages/MissionTasksPage/MissionTasksPage";
import { MissionsListPage } from "./pages/MissionsListPage/MissionsListPage";
import { NewHeroPage } from "./pages/NewHeroPage/NewHeroPage";
import { NewPlayerMissionPage } from "./pages/NewPlayerMissionPage/NewPlayerMissionPage";
import { NewTeamPage } from "./pages/NewTeamPage/NewTeamPage";
import { SabotagePage } from "./pages/SabotagePage/SabotagePage";
import { TeamDetailPage } from "./pages/TeamDetailPage/TeamDetailPage";
import { TeamsListPage } from "./pages/TeamsListPage/TeamsListPage";
import { VillainOpsPage } from "./pages/VillainOpsPage/VillainOpsPage";

function AuthenticatedApp() {
  const { user } = useAuth();

  if (user?.role === "VILLAIN") {
    return (
      <VillainLayout>
        <Routes>
          <Route path="/" element={<Navigate to="/vilao/ops" replace />} />
          <Route path="/vilao/ops" element={<VillainOpsPage />} />
          <Route path="/vilao/duelo/:duelId" element={<DuelArenaPage />} />
          <Route path="/vilao/duelo/:duelId/sabotagem" element={<SabotagePage />} />
          <Route path="*" element={<Navigate to="/vilao/ops" replace />} />
        </Routes>
      </VillainLayout>
    );
  }

  if (user?.role === "HERO") {
    return (
      <HeroLayout>
        <Routes>
          <Route path="/" element={<Navigate to="/heroi/area" replace />} />
          <Route path="/heroi/area" element={<HeroAreaPage />} />
          <Route path="/herois/:id" element={<HeroDetailPage />} />
          <Route path="/missoes" element={<MissionsListPage />} />
          <Route path="/missoes/nova" element={<NewPlayerMissionPage />} />
          <Route path="/missoes/:id" element={<MissionCommandPage />} />
          <Route path="/missoes/:id/tarefas" element={<MissionTasksPage />} />
          <Route path="/duelo/:duelId" element={<DuelArenaPage />} />
          <Route path="/missoes-jogador/*" element={<Navigate to="/missoes" replace />} />
          <Route path="/heroi/minhas-missoes" element={<Navigate to="/missoes" replace />} />
          <Route path="*" element={<Navigate to="/heroi/area" replace />} />
        </Routes>
      </HeroLayout>
    );
  }

  return (
    <AppLayout>
      <Routes>
        <Route path="/heroi/*" element={<Navigate to="/" replace />} />
        <Route path="/vilao/*" element={<Navigate to="/" replace />} />
        <Route path="/" element={<DashboardPage />} />
        <Route path="/missoes" element={<MissionsListPage />} />
        <Route path="/missoes/:id" element={<MissionCommandPage />} />
        <Route path="/missoes/:id/tarefas" element={<MissionTasksPage />} />
        <Route path="/herois" element={<HeroesListPage />} />
        <Route path="/herois/nova" element={<NewHeroPage />} />
        <Route path="/herois/:id" element={<HeroDetailPage />} />
        <Route path="/equipes" element={<TeamsListPage />} />
        <Route path="/equipes/nova" element={<NewTeamPage />} />
        <Route path="/equipes/:id" element={<TeamDetailPage />} />
        <Route path="/operacoes/nova" element={<Navigate to="/missoes" replace />} />
        <Route path="/missoes-jogador/*" element={<Navigate to="/missoes" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppLayout>
  );
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/cadastro" element={<RegisterPage />} />
      <Route element={<RequireAuth />}>
        <Route path="/*" element={<AuthenticatedApp />} />
      </Route>
    </Routes>
  );
}
