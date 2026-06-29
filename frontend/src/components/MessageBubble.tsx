import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { formatBytes } from '../lib/chat';
import type { MessageItem } from '../types/app';

type MessageBubbleProps = {
  message: MessageItem;
};

export function MessageBubble({ message }: MessageBubbleProps) {
  const isAssistant = message.role === 'assistant';
  const attachments = message.attachments ?? [];

  return (
    <article className={`message-card${isAssistant ? ' assistant' : ''}`}>
      <div className="message-meta">
        <span className="message-role">{isAssistant ? 'Claude' : 'You'}</span>
        <span className="message-type">{message.role}</span>
      </div>

      {message.content ? (
        <div className="markdown-body">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.content}</ReactMarkdown>
        </div>
      ) : null}

      {attachments.length > 0 ? (
        <div className="attachment-grid">
          {attachments.map((attachment) => (
            <div key={attachment.fileId} className="attachment-card">
              {attachment.url && attachment.fileKind === 'image' ? (
                <img src={attachment.url} alt={attachment.originalFilename} loading="lazy" />
              ) : null}
              <div className="attachment-copy">
                <strong>{attachment.originalFilename}</strong>
                <span>
                  {[attachment.contentType, formatBytes(attachment.sizeBytes)]
                    .filter(Boolean)
                    .join(' · ')}
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
    </article>
  );
}
