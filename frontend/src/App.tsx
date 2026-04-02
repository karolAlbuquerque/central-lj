import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth/AuthContext";
import { RequireAuth } from "./components/RequireAuth/RequireAuth";
import { AppLayout } from "./layouts/AppLayout/AppLayout";
import { HeroLayout } from "./layouts/HeroLayout/HeroLayout";
import { DashboardPage } from "./pages/DashboardPage/DashboardPage";
import { HeroAreaPage } from "./pages/HeroAreaPage/HeroAreaPage";
import { HeroDetailPage } from "./pages/HeroDetailPage/HeroDetailPage";
import { HeroMissionsPage } from "./pages/HeroMissionsPage/HeroMissionsPage";
import { HeroesListPage } from "./pages/HeroesListPage/HeroesListPage";
import { LoginPage } from "./pages/LoginPage/LoginPage";
import { MissionDetailPage } from "./pages/MissionDetailPage/MissionDetailPage";
import { MissionsListPage } from "./pages/MissionsListPage/MissionsListPage";
import { NewHeroPage } from "./pages/NewHeroPage/NewHeroPage";
import { NewMissionPage } from "./pages/NewMissionPage/NewMissionPage";
import { NewTeamPage } from "./pages/NewTeamPage/NewTeamPage";
import { TeamDetailPage } from "./pages/TeamDetailPage/TeamDetailPage";
import { TeamsListPage } from "./pages/TeamsListPage/TeamsListPage";

function AuthenticatedApp() {
  const { user } = useAuth();
  const isHero = user?.role === "HERO";

  if (isHero) {
    return (
      <HeroLayout>
        <Routes>
          <Route path="/" element={<Navigate to="/heroi/area" replace />} />
          <Route path="/heroi/area" element={<HeroAreaPage />} />
          <Route path="/heroi/minhas-missoes" element={<HeroMissionsPage />} />
          <Route path="/missoes/:id" element={<MissionDetailPage />} />
          <Route path="/herois/:id" element={<HeroDetailPage />} />
          <Route path="*" element={<Navigate to="/heroi/area" replace />} />
        </Routes>
      </HeroLayout>
    );
  }

  return (
    <AppLayout>
      <Routes>
        <Route path="/heroi/*" element={<Navigate to="/" replace />} />
        <Route path="/" element={<DashboardPage />} />
        <Route path="/missoes" element={<MissionsListPage />} />
        <Route path="/missoes/:id" element={<MissionDetailPage />} />
        <Route path="/operacoes/nova" element={<NewMissionPage />} />
        <Route path="/herois" element={<HeroesListPage />} />
        <Route path="/herois/nova" element={<NewHeroPage />} />
        <Route path="/herois/:id" element={<HeroDetailPage />} />
        <Route path="/equipes" element={<TeamsListPage />} />
        <Route path="/equipes/nova" element={<NewTeamPage />} />
        <Route path="/equipes/:id" element={<TeamDetailPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppLayout>
  );
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route path="/*" element={<AuthenticatedApp />} />
      </Route>
    </Routes>
  );
}
