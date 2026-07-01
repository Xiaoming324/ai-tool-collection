import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { ProtectedRoute } from './components/ProtectedRoute';
import { useAuth } from './contexts/AuthContext';
import { ChatPage } from './pages/ChatPage';
import { LoginPage } from './pages/LoginPage';
import { ModuleHubPage } from './pages/ModuleHubPage';
import { PdfPage } from './pages/PdfPage';
import { RegisterPage } from './pages/RegisterPage';
import { TravelPage } from './pages/TravelPage';

function AuthRedirect() {
  const { isAuthenticated } = useAuth();
  return <Navigate to={isAuthenticated ? '/app' : '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<AuthRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/app" element={<ModuleHubPage />} />
        <Route path="/app" element={<AppShell />}>
          <Route path="chat" element={<ChatPage />} />
          <Route path="pdf" element={<PdfPage />} />
          <Route path="travel" element={<TravelPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
