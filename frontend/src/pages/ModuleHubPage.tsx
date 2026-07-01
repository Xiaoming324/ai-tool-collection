import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import type { NavModule } from '../types/app';

type HubModule = NavModule & {
  icon: string;
};

const modules: HubModule[] = [
  {
    type: 'chat',
    path: '/app/chat',
    label: 'AI Chat',
    summary: 'Text + Image',
    note: 'Multimodal chat with persistent history and image uploads.',
    icon: 'CH',
  },
  {
    type: 'pdf',
    path: '/app/pdf',
    label: 'ChatPDF',
    summary: 'Upload + Ask',
    note: 'Upload PDFs, preview files, and ask document-grounded questions.',
    icon: 'PDF',
  },
  {
    type: 'travel',
    path: '/app/travel',
    label: 'Travel Assistant',
    summary: 'Plan + Save',
    note: 'Plan trips with tools, then save and reopen itineraries.',
    icon: 'TR',
  },
];

export function ModuleHubPage() {
  const navigate = useNavigate();
  const { signOut, username } = useAuth();

  function handleSignOut() {
    signOut();
    navigate('/login');
  }

  return (
    <section className="hub-page">
      <nav className="hub-nav">
        <NavLink to="/app" className="hub-brand">
          <span className="hub-brand-mark">AI</span>
          <strong>AI Tool Collection</strong>
        </NavLink>
        <div className="hub-actions">
          <span>{username}</span>
          <button type="button" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      </nav>

      <header className="hub-hero">
        <p className="section-eyebrow">Application Center</p>
        <h2>AI Tool Collection</h2>
        <p>Choose a focused workspace and jump into the right AI workflow.</p>
      </header>

      <div className="hub-grid">
        {modules.map((module) => (
          <NavLink key={`${module.path}-${module.label}`} to={module.path} className="hub-card">
            <div className="hub-icon">{module.icon}</div>
            <h3>{module.label}</h3>
            <span>{module.summary}</span>
            <p>{module.note}</p>
          </NavLink>
        ))}
      </div>
    </section>
  );
}
