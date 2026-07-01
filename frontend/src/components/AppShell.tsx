import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export function AppShell() {
  const navigate = useNavigate();
  const { signOut, username } = useAuth();

  function handleSignOut() {
    signOut();
    navigate('/login');
  }

  return (
    <div className="workspace-shell">
      <header className="workspace-topbar">
        <NavLink to="/app" className="workspace-back">
          <span className="brand-mark small">AI</span>
          <span className="workspace-brand-copy">
            <strong>AI Tool Collection</strong>
            <small>Back to app center</small>
          </span>
        </NavLink>

        <div className="workspace-actions">
          <span>{username}</span>
          <button type="button" className="ghost-button" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      </header>

      <main className="workspace-body">
        <Outlet />
      </main>
    </div>
  );
}
