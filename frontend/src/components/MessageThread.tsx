import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { formatBytes, toMessageRoleLabel } from '../lib/chat';
import type { MessageItem } from '../types/app';

type MessageThreadProps = {
  messages: MessageItem[];
  loading: boolean;
  emptyTitle: string;
  emptyDescription: string;
  typing: boolean;
};

export function MessageThread({
  messages,
  loading,
  emptyTitle,
  emptyDescription,
  typing,
}: MessageThreadProps) {
  if (loading) {
    return <div className="empty-panel compact">Loading messages...</div>;
  }

  if (messages.length === 0) {
    return (
      <div className="empty-panel">
        <p className="section-eyebrow">Ready</p>
        <h3>{emptyTitle}</h3>
        <p>{emptyDescription}</p>
      </div>
    );
  }

  return (
    <div className="thread-stack">
      {messages.map((message, index) => {
        const assistant = message.role === 'assistant';
        return (
          <article key={`${message.role}-${index}-${message.content.length}`} className={`message-row${assistant ? ' assistant' : ' user'}`}>
            <div className="message-avatar">{assistant ? 'LU' : 'ME'}</div>
            <div className="message-card">
              <div className="message-top">
                <span className="message-author">{toMessageRoleLabel(message.role)}</span>
                <span className="message-role">{message.role}</span>
              </div>

              {message.content ? (
                <div className="markdown-body">
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.content}</ReactMarkdown>
                </div>
              ) : null}

              {typing && assistant && index === messages.length - 1 && !message.content ? (
                <div className="typing-dots">
                  <span />
                  <span />
                  <span />
                </div>
              ) : null}

              {message.attachments.length > 0 ? (
                <div className="attachment-grid">
                  {message.attachments.map((attachment) => (
                    <div key={attachment.fileId} className="attachment-card">
                      {attachment.url && attachment.fileKind === 'image' ? (
                        <img src={attachment.url} alt={attachment.originalFilename} loading="lazy" />
                      ) : null}
                      <div className="attachment-copy">
                        <strong>{attachment.originalFilename}</strong>
                        <span>
                          {[attachment.contentType, formatBytes(attachment.sizeBytes)]
                            .filter(Boolean)
                            .join(' / ')}
                        </span>
                        {attachment.url ? (
                          <a href={attachment.url} target="_blank" rel="noreferrer">
                            Open file
                          </a>
                        ) : null}
                      </div>
                    </div>
                  ))}
                </div>
              ) : null}
            </div>
          </article>
        );
      })}
    </div>
  );
}
