import { useState } from 'react';
import { ConfirmDialog } from './ConfirmDialog';
import { getSessionLabel } from '../lib/chat';
import type { SessionSummary } from '../types/app';

type SessionRailProps = {
  title: string;
  sessions: SessionSummary[];
  activeChatId: string;
  loading: boolean;
  onSelect: (chatId: string) => void;
  onCreate: () => void;
  onDelete: (session: SessionSummary) => Promise<void> | void;
};

export function SessionRail({
  title,
  sessions,
  activeChatId,
  loading,
  onSelect,
  onCreate,
  onDelete,
}: SessionRailProps) {
  const [pendingDelete, setPendingDelete] = useState<SessionSummary | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleConfirmDelete() {
    if (!pendingDelete) {
      return;
    }

    setDeleting(true);
    try {
      await onDelete(pendingDelete);
      setPendingDelete(null);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
      <aside className="session-rail">
        <div className="section-head">
          <div>
            <p className="section-eyebrow">Sessions</p>
            <h2>{title}</h2>
          </div>
          <button type="button" className="outline-button" onClick={onCreate}>
            New
          </button>
        </div>

        <div className="session-stack">
          {loading ? <p className="panel-note">Loading sessions...</p> : null}

          {!loading && sessions.length === 0 ? (
            <p className="panel-note">No sessions yet. Start the first one.</p>
          ) : null}

          {sessions.map((session) => {
            const active = session.chatId === activeChatId;
            return (
              <div key={session.chatId} className={`session-card${active ? ' active' : ''}`}>
                <button type="button" className="session-select" onClick={() => onSelect(session.chatId)}>
                  <strong>{getSessionLabel(session)}</strong>
                  <span>{active ? 'Open now' : 'Open conversation'}</span>
                </button>
                <button
                  type="button"
                  className="session-delete"
                  onClick={() => setPendingDelete(session)}
                  aria-label={`Delete ${getSessionLabel(session)}`}
                >
                  Delete
                </button>
              </div>
            );
          })}
        </div>
      </aside>

      <ConfirmDialog
        open={Boolean(pendingDelete)}
        title={`Delete "${pendingDelete ? getSessionLabel(pendingDelete) : ''}"?`}
        description="This conversation and its related files will be removed. This action cannot be undone."
        confirmLabel="Delete session"
        busy={deleting}
        onCancel={() => {
          if (!deleting) {
            setPendingDelete(null);
          }
        }}
        onConfirm={() => void handleConfirmDelete()}
      />
    </>
  );
}
