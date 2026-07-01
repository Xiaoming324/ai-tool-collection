import { startTransition, useEffect, useRef, useState } from 'react';
import { Composer } from '../components/Composer';
import { MessageThread } from '../components/MessageThread';
import { SessionRail } from '../components/SessionRail';
import { useAuth } from '../contexts/AuthContext';
import { deleteSession, getMessages, listSessions, streamChatMessage } from '../lib/api';
import { createChatId, makeEmptyAssistantMessage } from '../lib/chat';
import { readTextStream } from '../lib/stream';
import type { MessageAttachment, MessageItem, SessionSummary } from '../types/app';

export function ChatPage() {
  const { token, signOut } = useAuth();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [activeChatId, setActiveChatId] = useState(() => createChatId('chat'));
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [prompt, setPrompt] = useState('');
  const [files, setFiles] = useState<File[]>([]);
  const [loadingSessions, setLoadingSessions] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [streaming, setStreaming] = useState(false);
  const [error, setError] = useState('');
  const endRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    void loadSessions();
  }, []);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages]);

  useEffect(() => {
    if (!sessions.some((session) => session.chatId === activeChatId)) {
      setMessages([]);
      return;
    }
    void loadMessages(activeChatId);
  }, [activeChatId, sessions]);

  async function loadSessions() {
    if (!token) {
      return;
    }

    setLoadingSessions(true);
    try {
      const data = await listSessions('chat', token);
      startTransition(() => {
        setSessions(data);
      });
    } catch (err) {
      handleError(err, 'Unable to load chat sessions.');
    } finally {
      setLoadingSessions(false);
    }
  }

  async function loadMessages(chatId: string) {
    if (!token) {
      return;
    }

    setLoadingMessages(true);
    setError('');
    try {
      const data = await getMessages('chat', chatId, token);
      startTransition(() => {
        setMessages(data);
      });
    } catch (err) {
      handleError(err, 'Unable to load messages.');
    } finally {
      setLoadingMessages(false);
    }
  }

  function handleError(err: unknown, fallback: string) {
    const message = err instanceof Error ? err.message : fallback;
    if (message.toLowerCase().includes('unauthorized')) {
      signOut();
      return;
    }
    setError(message || fallback);
  }

  function handleCreate() {
    startTransition(() => {
      setActiveChatId(createChatId('chat'));
      setMessages([]);
      setPrompt('');
      setFiles([]);
      setError('');
    });
  }

  async function handleDelete(chatId: string) {
    if (!token) {
      return;
    }

    try {
      await deleteSession('chat', chatId, token);
      const remaining = sessions.filter((session) => session.chatId !== chatId);
      startTransition(() => {
        setSessions(remaining);
        if (activeChatId === chatId) {
          setActiveChatId(createChatId('chat'));
          setMessages([]);
        }
      });
    } catch (err) {
      handleError(err, 'Unable to delete this session.');
    }
  }

  async function handleSubmit() {
    if (!token || streaming) {
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

    const userMessage: MessageItem = {
      role: 'user',
      content: normalizedPrompt,
      attachments: stagedAttachments,
    };

    setMessages((current) => [...current, userMessage, makeEmptyAssistantMessage()]);
    setPrompt('');
    setFiles([]);
    setError('');
    setStreaming(true);

    let assistantText = '';

    try {
      const response = await streamChatMessage(token, activeChatId, normalizedPrompt, files);
      await readTextStream(response, (chunk) => {
        assistantText += chunk;
        setMessages((current) => {
          const next = [...current];
          const last = next[next.length - 1];
          if (last && last.role === 'assistant') {
            last.content = assistantText;
          }
          return next;
        });
      });

      await loadSessions();
      await loadMessages(activeChatId);
    } catch (err) {
      handleError(err, 'Unable to send chat message.');
      setMessages((current) => {
        const next = [...current];
        const last = next[next.length - 1];
        if (last?.role === 'assistant') {
          last.content = err instanceof Error ? `Request failed: ${err.message}` : 'Request failed.';
        }
        return next;
      });
    } finally {
      setStreaming(false);
    }
  }

  return (
    <div className="module-layout">
      <SessionRail
        title="Chat"
        sessions={sessions}
        activeChatId={activeChatId}
        loading={loadingSessions}
        onSelect={setActiveChatId}
        onCreate={handleCreate}
        onDelete={(session) => handleDelete(session.chatId)}
      />

      <section className="module-panel">
        <header className="module-header">
          <div>
            <p className="section-eyebrow">Text + Image</p>
            <h2>Persistent multimodal conversation</h2>
          </div>
          <p>
            Ask naturally, attach images when needed, and reopen the same thread without losing context.
          </p>
        </header>

        {error ? <div className="alert-banner">{error}</div> : null}

        <div className="thread-shell">
          <MessageThread
            messages={messages}
            loading={loadingMessages}
            emptyTitle="Start a new multimodal thread"
            emptyDescription="Ask Lumi anything, or attach screenshots and images for analysis."
            typing={streaming}
          />
          <div ref={endRef} />
        </div>

        <footer className="module-footer">
          <Composer
            prompt={prompt}
            files={files}
            disabled={streaming}
            allowFiles
            accept="image/*"
            placeholder="Message Lumi. Attach one or more images if you want visual analysis."
            submitLabel={streaming ? 'Streaming...' : 'Send'}
            onPromptChange={setPrompt}
            onFilesChange={setFiles}
            onSubmit={handleSubmit}
          />
        </footer>
      </section>
    </div>
  );
}
