import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "./layouts/AppLayout/AppLayout";
import { DashboardPage } from "./pages/DashboardPage/DashboardPage";
import { MissionDetailPage } from "./pages/MissionDetailPage/MissionDetailPage";
import { NewMissionPage } from "./pages/NewMissionPage/NewMissionPage";

export function App() {
  return (
    <BrowserRouter>
      <AppLayout>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/operacoes/nova" element={<NewMissionPage />} />
          <Route path="/missoes/:id" element={<MissionDetailPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AppLayout>
    </BrowserRouter>
  );
}
