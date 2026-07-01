import { startTransition, useEffect, useRef, useState } from 'react';
import { Composer } from '../components/Composer';
import { MessageThread } from '../components/MessageThread';
import { SessionRail } from '../components/SessionRail';
import { useAuth } from '../contexts/AuthContext';
import {
  deleteSession,
  getMessages,
  getPdfSessionFile,
  listSessions,
  streamPdfMessage,
  uploadPdf,
} from '../lib/api';
import { createChatId, makeEmptyAssistantMessage } from '../lib/chat';
import { readTextStream } from '../lib/stream';
import type { MessageItem, SessionSummary } from '../types/app';

export function PdfPage() {
  const { token, signOut } = useAuth();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [activeChatId, setActiveChatId] = useState(() => createChatId('pdf'));
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [prompt, setPrompt] = useState('');
  const [loadingSessions, setLoadingSessions] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [streaming, setStreaming] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [previewUrl, setPreviewUrl] = useState('');
  const [previewName, setPreviewName] = useState('');
  const [previewReady, setPreviewReady] = useState(false);
  const uploadInputRef = useRef<HTMLInputElement | null>(null);
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
      setPreviewUrl('');
      setPreviewName('');
      setPreviewReady(false);
      return;
    }

    void loadPreview(activeChatId);
    void loadMessages(activeChatId);
  }, [activeChatId, sessions]);

  async function loadSessions() {
    if (!token) {
      return;
    }

    setLoadingSessions(true);
    try {
      const data = await listSessions('pdf', token);
      startTransition(() => {
        setSessions(data);
      });
    } catch (err) {
      handleError(err, 'Unable to load PDF sessions.');
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
      const data = await getMessages('pdf', chatId, token);
      startTransition(() => {
        setMessages(data);
      });
    } catch (err) {
      handleError(err, 'Unable to load PDF conversation.');
    } finally {
      setLoadingMessages(false);
    }
  }

  async function loadPreview(chatId: string) {
    if (!token) {
      return;
    }

    try {
      const data = await getPdfSessionFile(token, chatId);
      setPreviewUrl(data.url);
      setPreviewName(data.originalFilename);
      setPreviewReady(true);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to load PDF preview.';
      if (message.toLowerCase().includes('no pdf file found')) {
        setPreviewUrl('');
        setPreviewName('');
        setPreviewReady(false);
        return;
      }
      handleError(err, 'Unable to load PDF preview.');
      setPreviewReady(false);
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
      setActiveChatId(createChatId('pdf'));
      setMessages([]);
      setPrompt('');
      setError('');
      setPreviewUrl('');
      setPreviewName('');
      setPreviewReady(false);
    });
  }

  async function handleDelete(chatId: string) {
    if (!token) {
      return;
    }

    try {
      await deleteSession('pdf', chatId, token);
      const remaining = sessions.filter((session) => session.chatId !== chatId);
      startTransition(() => {
        setSessions(remaining);
        if (activeChatId === chatId) {
          setActiveChatId(createChatId('pdf'));
          setMessages([]);
          setPreviewUrl('');
          setPreviewName('');
          setPreviewReady(false);
        }
      });
    } catch (err) {
      handleError(err, 'Unable to delete this PDF session.');
    }
  }

  async function handleUpload(file: File) {
    if (!token) {
      return;
    }

    setUploading(true);
    setError('');
    try {
      const result = await uploadPdf(token, activeChatId, file);
      setPreviewName(result.originalFilename);
      await loadSessions();
      setActiveChatId(result.chatId);
      await loadPreview(result.chatId);
    } catch (err) {
      handleError(err, 'Unable to upload PDF.');
    } finally {
      setUploading(false);
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

    const userMessage: MessageItem = {
      role: 'user',
      content: normalizedPrompt,
      attachments: [],
    };

    setMessages((current) => [...current, userMessage, makeEmptyAssistantMessage()]);
    setPrompt('');
    setError('');
    setStreaming(true);

    let assistantText = '';

    try {
      const response = await streamPdfMessage(token, activeChatId, normalizedPrompt);
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
      handleError(err, 'Unable to send PDF question.');
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
    <div className="module-layout pdf-layout">
      <SessionRail
        title="ChatPDF"
        sessions={sessions}
        activeChatId={activeChatId}
        loading={loadingSessions}
        onSelect={setActiveChatId}
        onCreate={handleCreate}
        onDelete={(session) => handleDelete(session.chatId)}
      />

      <section className="pdf-shell">
        <div className="pdf-preview-pane">
          <header className="pdf-preview-head">
            <div>
              <p className="section-eyebrow">Document Preview</p>
              <h2>{previewName || 'No PDF selected'}</h2>
            </div>
            <div className="pdf-actions">
              <input
                ref={uploadInputRef}
                type="file"
                hidden
                accept="application/pdf,.pdf"
                onChange={(event) => {
                  const file = event.target.files?.[0];
                  if (file) {
                    void handleUpload(file);
                  }
                  event.target.value = '';
                }}
              />
              <button
                type="button"
                className="primary-button"
                disabled={uploading}
                onClick={() => uploadInputRef.current?.click()}
              >
                {uploading ? 'Uploading...' : 'Upload PDF'}
              </button>
            </div>
          </header>

          <div className="pdf-canvas">
            {previewReady && previewUrl ? (
              <iframe title={previewName || activeChatId} src={previewUrl} className="pdf-frame" />
            ) : (
              <div className="empty-panel pdf-empty">
                <p className="section-eyebrow">RAG Workspace</p>
                <h3>Upload a PDF to start this session</h3>
                <p>Once the file is uploaded, the left side keeps the preview and the right side stays focused on questions.</p>
              </div>
            )}
          </div>
        </div>

        <section className="module-panel">
          <header className="module-header compact">
            <div>
              <p className="section-eyebrow">Retrieval-Augmented Chat</p>
              <h2>Question the uploaded document</h2>
            </div>
            <p>
              Ask for summaries, evidence, methods, or findings while keeping the document visible beside the chat.
            </p>
          </header>

          {error ? <div className="alert-banner">{error}</div> : null}

          <div className="thread-shell">
            <MessageThread
              messages={messages}
              loading={loadingMessages}
              emptyTitle="Your PDF conversation will appear here"
              emptyDescription="Upload a document, then ask a question such as summary, methodology, or key findings."
              typing={streaming}
            />
            <div ref={endRef} />
          </div>

          <footer className="module-footer">
            <Composer
              prompt={prompt}
              disabled={streaming || uploading}
              placeholder="Ask about the uploaded PDF..."
              submitLabel={streaming ? 'Streaming...' : 'Ask PDF'}
              onPromptChange={setPrompt}
              onSubmit={handleSubmit}
            />
          </footer>
        </section>
      </section>
    </div>
  );
}
