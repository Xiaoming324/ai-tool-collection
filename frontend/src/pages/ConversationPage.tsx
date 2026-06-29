import { startTransition, useEffect, useRef, useState } from 'react';
import { MessageBubble } from '../components/MessageBubble';
import { MessageComposer } from '../components/MessageComposer';
import { SessionList } from '../components/SessionList';
import { useAuth } from '../contexts/AuthContext';
import { createChatId } from '../lib/chat';
import { createStreamRequest, getMessages, listSessions } from '../lib/api';
import { readTextStream } from '../lib/stream';
import type { MessageAttachment, MessageItem, ModuleDefinition, SessionSummary } from '../types/app';

type ConversationPageProps = {
  module: ModuleDefinition;
};

export function ConversationPage({ module }: ConversationPageProps) {
  const { token, signOut } = useAuth();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [activeChatId, setActiveChatId] = useState(() => createChatId(module.type));
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [prompt, setPrompt] = useState('');
  const [files, setFiles] = useState<File[]>([]);
  const [isSessionsLoading, setIsSessionsLoading] = useState(true);
  const [isMessagesLoading, setIsMessagesLoading] = useState(false);
  const [isStreaming, setIsStreaming] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    void refreshSessions(true);
    startTransition(() => {
      setActiveChatId(createChatId(module.type));
      setMessages([]);
      setPrompt('');
      setFiles([]);
      setError('');
    });
  }, [module.type]);

  useEffect(() => {
    if (!sessions.some((session) => session.chatId === activeChatId)) {
      setMessages([]);
      return;
    }

    void refreshMessages(activeChatId);
  }, [activeChatId, sessions]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages]);

  async function refreshSessions(showLoader = false) {
    if (!token) {
      return;
    }

    if (showLoader) {
      setIsSessionsLoading(true);
    }

    try {
      const data = await listSessions(module.type, token);
      startTransition(() => {
        setSessions(data);
      });
    } catch (err) {
      handleRequestError(err, `Unable to load ${module.label.toLowerCase()} history.`);
    } finally {
      setIsSessionsLoading(false);
    }
  }

  async function refreshMessages(chatId: string) {
    if (!token) {
      return;
    }

    setIsMessagesLoading(true);
    setError('');

    try {
      const data = await getMessages(module.type, chatId, token);
      startTransition(() => {
        setMessages(data);
      });
    } catch (err) {
      handleRequestError(err, 'Unable to load this conversation.');
    } finally {
      setIsMessagesLoading(false);
    }
  }

  function handleRequestError(err: unknown, fallback: string) {
    const message = err instanceof Error ? err.message : fallback;
    if (message.toLowerCase().includes('unauthorized')) {
      signOut();
      return;
    }
    setError(message || fallback);
  }

  function handleCreate() {
    startTransition(() => {
      setActiveChatId(createChatId(module.type));
      setMessages([]);
      setPrompt('');
      setFiles([]);
      setError('');
    });
  }

  async function handleSubmit() {
    if (!token || isStreaming) {
      return;
    }

    const normalizedPrompt = prompt.trim();
    if (!normalizedPrompt) {
      return;
    }

    const stagedAttachments = files.map<MessageAttachment>((file, index) => ({
      fileId: -(index + 1),
      fileKind: file.type.startsWith('image/') ? 'image' : 'file',
      originalFilename: file.name,
      contentType: file.type,
      sizeBytes: file.size,
      url: file.type.startsWith('image/') ? URL.createObjectURL(file) : undefined,
    }));

    const nextMessages: MessageItem[] = [
      ...messages,
      {
        role: 'user',
        content: normalizedPrompt,
        attachments: stagedAttachments,
      },
      {
        role: 'assistant',
        content: '',
      },
    ];

    setMessages(nextMessages);
    setPrompt('');
    setFiles([]);
    setError('');
    setIsStreaming(true);

    let assistantReply = '';

    try {
      const response = await createStreamRequest(
        module.endpoint,
        {
          chatId: activeChatId,
          prompt: normalizedPrompt,
          files,
        },
        token,
        module.requestMethod,
      );

      await readTextStream(response, (chunk) => {
        assistantReply += chunk;
        setMessages((current) => {
          const draft = [...current];
          const last = draft[draft.length - 1];
          if (last && last.role === 'assistant') {
            last.content = assistantReply;
          }
          return draft;
        });
      });

      await refreshSessions();
      await refreshMessages(activeChatId);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Streaming request failed.';
      setMessages((current) => {
        const draft = [...current];
        const last = draft[draft.length - 1];
        if (last?.role === 'assistant') {
          last.content = module.type === 'travel' && message.includes('404')
            ? 'The travel backend endpoint is not available yet. The frontend contract is ready and waiting for `/ai/travel`.'
            : `Request failed: ${message}`;
        }
        return draft;
      });
      handleRequestError(err, 'Streaming request failed.');
    } finally {
      setIsStreaming(false);
    }
  }

  const hasPersistedSession = sessions.some((session) => session.chatId === activeChatId);
  const showEmptyState = !isMessagesLoading && messages.length === 0;

  return (
    <div className="workspace">
      <SessionList
        sessions={sessions}
        activeChatId={activeChatId}
        isLoading={isSessionsLoading}
        onSelect={setActiveChatId}
        onCreate={handleCreate}
        title={module.label}
        subtitle={module.kicker}
      />

      <section className="conversation-pane">
        <header className="pane-hero">
          <div>
            <p className="eyebrow">{module.kicker}</p>
            <h2>{module.heading}</h2>
          </div>
          <p>{module.description}</p>
        </header>

        {error ? <div className="alert-strip">{error}</div> : null}

        <div className="message-scroll">
          {showEmptyState ? (
            <div className="empty-state">
              <p className="eyebrow">{module.label}</p>
              <h3>{module.emptyTitle}</h3>
              <p>{module.emptyDescription}</p>
              <div className="empty-meta">
                <span>{hasPersistedSession ? activeChatId : 'New thread'}</span>
                <span>{module.acceptsImages ? 'Text + image' : 'Text stream'}</span>
              </div>
            </div>
          ) : null}

          {isMessagesLoading ? <div className="empty-state compact">Loading messages...</div> : null}

          {messages.map((message, index) => (
            <MessageBubble key={`${message.role}-${index}-${message.content.length}`} message={message} />
          ))}

          <div ref={messagesEndRef} />
        </div>

        <footer className="pane-footer">
          <MessageComposer
            acceptsImages={module.acceptsImages}
            disabled={isStreaming || module.type === 'pdf'}
            prompt={prompt}
            files={files}
            placeholder={module.composerPlaceholder}
            onPromptChange={setPrompt}
            onFilesChange={setFiles}
            onSubmit={handleSubmit}
          />
          <p className="composer-hint">{module.hint}</p>
        </footer>
      </section>
    </div>
  );
}
