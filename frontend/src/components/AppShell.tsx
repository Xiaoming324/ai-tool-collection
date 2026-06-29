import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const moduleLinks = [
  {
    to: '/app/chat',
    label: 'Multimodal Chat',
    badge: 'M2',
    note: 'Images + persistent memory',
  },
  {
    to: '/app/pdf',
    label: 'ChatPDF',
    badge: 'M3',
    note: 'Document RAG is next',
  },
  {
    to: '/app/travel',
    label: 'Travel Assistant',
    badge: 'M4',
    note: 'Function calling surface',
  },
];

export function AppShell() {
  const navigate = useNavigate();
  const location = useLocation();
  const { signOut, username } = useAuth();

  function handleSignOut() {
    signOut();
    navigate('/login');
  }

  return (
    <div className="shell">
      <aside className="shell-nav">
        <div className="brand-block">
          <div className="brand-mark">AT</div>
          <div>
            <p className="eyebrow">AI Tool Collection</p>
            <h1>Operational cockpit</h1>
          </div>
        </div>

        <div className="module-stack">
          {moduleLinks.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `module-link${isActive ? ' active' : ''}`}
            >
              <div className="module-link-top">
                <span>{item.label}</span>
                <span className="module-badge">{item.badge}</span>
              </div>
              <small>{item.note}</small>
            </NavLink>
          ))}
        </div>

        <div className="status-card">
          <p className="eyebrow">Current focus</p>
          <strong>{location.pathname.includes('/travel') ? 'Module 4' : location.pathname.includes('/pdf') ? 'Module 3' : 'Module 2'}</strong>
          <span>
            {location.pathname.includes('/travel')
              ? 'Frontend is wired for tool-calling chat.'
              : location.pathname.includes('/pdf')
                ? 'Document workspace is staged for the next backend slice.'
                : 'Chat sessions, memory, S3 uploads, and signed image history are live.'}
          </span>
        </div>

        <div className="user-card">
          <div className="user-avatar">{username?.slice(0, 1).toUpperCase() ?? 'U'}</div>
          <div>
            <strong>{username}</strong>
            <span>Signed in</span>
          </div>
          <button type="button" className="ghost-button" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      </aside>

      <main className="shell-main">
        <Outlet />
      </main>
    </div>
  );
}
