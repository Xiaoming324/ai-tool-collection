import { getSessionTitle } from '../lib/chat';
import type { SessionSummary } from '../types/app';

type SessionListProps = {
  sessions: SessionSummary[];
  activeChatId: string;
  isLoading: boolean;
  onSelect: (chatId: string) => void;
  onCreate: () => void;
  title: string;
  subtitle: string;
};

export function SessionList({
  sessions,
  activeChatId,
  isLoading,
  onSelect,
  onCreate,
  title,
  subtitle,
}: SessionListProps) {
  return (
    <aside className="session-panel">
      <div className="session-panel-head">
        <div>
          <p className="eyebrow">{subtitle}</p>
          <h2>{title}</h2>
        </div>
        <button type="button" className="outline-button" onClick={onCreate}>
          New
        </button>
      </div>

      <div className="session-list">
        {isLoading ? <p className="session-state">Loading conversations...</p> : null}

        {!isLoading && sessions.length === 0 ? (
          <p className="session-state">No conversations yet. Start the first one.</p>
        ) : null}

        {sessions.map((session) => (
          <button
            key={session.chatId}
            type="button"
            className={`session-item${session.chatId === activeChatId ? ' active' : ''}`}
            onClick={() => onSelect(session.chatId)}
          >
            <strong>{getSessionTitle(session.chatId, session.title)}</strong>
            <span>{session.chatId}</span>
          </button>
        ))}
      </div>
    </aside>
  );
}
