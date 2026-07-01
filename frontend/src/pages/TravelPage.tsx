import { startTransition, useEffect, useRef, useState } from 'react';
import { Composer } from '../components/Composer';
import { MessageThread } from '../components/MessageThread';
import { SessionRail } from '../components/SessionRail';
import { useAuth } from '../contexts/AuthContext';
import {
  deleteSession,
  getMessages,
  getTravelItinerary,
  listSessions,
  listTravelItineraries,
  streamTravelMessage,
} from '../lib/api';
import { createChatId, makeEmptyAssistantMessage } from '../lib/chat';
import { readTextStream } from '../lib/stream';
import type { MessageItem, SessionSummary, TravelItinerary } from '../types/app';

export function TravelPage() {
  const { token, signOut } = useAuth();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [activeChatId, setActiveChatId] = useState(() => createChatId('travel'));
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [prompt, setPrompt] = useState('');
  const [itineraries, setItineraries] = useState<TravelItinerary[]>([]);
  const [selectedItinerary, setSelectedItinerary] = useState<TravelItinerary | null>(null);
  const [loadingSessions, setLoadingSessions] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [loadingTrips, setLoadingTrips] = useState(true);
  const [loadingTripDetail, setLoadingTripDetail] = useState(false);
  const [streaming, setStreaming] = useState(false);
  const [error, setError] = useState('');
  const endRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    void Promise.all([loadSessions(), loadItineraries()]);
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
      const data = await listSessions('travel', token);
      startTransition(() => {
        setSessions(data);
      });
    } catch (err) {
      handleError(err, 'Unable to load travel sessions.');
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
      const data = await getMessages('travel', chatId, token);
      startTransition(() => {
        setMessages(data);
      });
    } catch (err) {
      handleError(err, 'Unable to load travel conversation.');
    } finally {
      setLoadingMessages(false);
    }
  }

  async function loadItineraries() {
    if (!token) {
      return;
    }

    setLoadingTrips(true);
    try {
      const data = await listTravelItineraries(token);
      startTransition(() => {
        setItineraries(data);
        if (selectedItinerary) {
          const nextSelected = data.find((item) => item.id === selectedItinerary.id) ?? null;
          setSelectedItinerary(nextSelected);
        }
      });
    } catch (err) {
      handleError(err, 'Unable to load saved itineraries.');
    } finally {
      setLoadingTrips(false);
    }
  }

  async function openItinerary(itineraryId: number) {
    if (!token) {
      return;
    }

    setLoadingTripDetail(true);
    try {
      const data = await getTravelItinerary(token, itineraryId);
      setSelectedItinerary(data);
    } catch (err) {
      handleError(err, 'Unable to load itinerary detail.');
    } finally {
      setLoadingTripDetail(false);
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
      setActiveChatId(createChatId('travel'));
      setMessages([]);
      setPrompt('');
      setError('');
    });
  }

  async function handleDelete(chatId: string) {
    if (!token) {
      return;
    }

    try {
      await deleteSession('travel', chatId, token);
      const remaining = sessions.filter((session) => session.chatId !== chatId);
      startTransition(() => {
        setSessions(remaining);
        if (activeChatId === chatId) {
          setActiveChatId(createChatId('travel'));
          setMessages([]);
        }
      });
      await loadItineraries();
    } catch (err) {
      handleError(err, 'Unable to delete this travel session.');
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
      const response = await streamTravelMessage(token, activeChatId, normalizedPrompt);
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

      await Promise.all([loadSessions(), loadMessages(activeChatId), loadItineraries()]);
    } catch (err) {
      handleError(err, 'Unable to send travel request.');
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
    <div className="module-layout travel-layout">
      <SessionRail
        title="Travel Assistant"
        sessions={sessions}
        activeChatId={activeChatId}
        loading={loadingSessions}
        onSelect={setActiveChatId}
        onCreate={handleCreate}
        onDelete={(session) => handleDelete(session.chatId)}
      />

      <section className="module-panel">
        <header className="module-header compact travel-header">
          <div>
            <p className="section-eyebrow">Function Calling</p>
            <h2>Travel planning workspace</h2>
          </div>
          <p>
            Describe the destination, dates, budget, or style of trip. Lumi can refine the plan and save it to the right-side shelf.
          </p>
        </header>

        {error ? <div className="alert-banner">{error}</div> : null}

        <div className="thread-shell">
          <MessageThread
            messages={messages}
            loading={loadingMessages}
            emptyTitle="Start a travel planning thread"
            emptyDescription="Try prompts like: Plan a 4-day Tokyo itinerary next month and save it."
            typing={streaming}
          />
          <div ref={endRef} />
        </div>

        <footer className="module-footer">
            <Composer
              prompt={prompt}
              disabled={streaming}
            placeholder="Where do you want to go, when, and what kind of trip do you want?"
            submitLabel={streaming ? 'Streaming...' : 'Plan trip'}
              onPromptChange={setPrompt}
              onSubmit={handleSubmit}
            />
        </footer>
      </section>

      <aside className="travel-sidepane">
        <div className="section-head">
          <div>
            <p className="section-eyebrow">My Trips</p>
            <h2>Saved itineraries</h2>
          </div>
        </div>

        <div className="trip-list">
          {loadingTrips ? <p className="panel-note">Loading itineraries...</p> : null}

          {!loadingTrips && itineraries.length === 0 ? (
            <p className="panel-note">No itineraries saved yet. Ask Lumi to save one for you.</p>
          ) : null}

          {itineraries.map((item) => (
            <button
              key={item.id}
              type="button"
              className={`trip-card${selectedItinerary?.id === item.id ? ' active' : ''}`}
              onClick={() => void openItinerary(item.id)}
            >
              <strong>{item.title}</strong>
              <span>{item.destination}</span>
              <small>
                {item.startDate || 'TBD'} - {item.endDate || 'TBD'}
              </small>
            </button>
          ))}
        </div>

        <div className="trip-detail">
          {loadingTripDetail ? <p className="panel-note">Loading trip detail...</p> : null}

          {!loadingTripDetail && !selectedItinerary ? (
            <div className="empty-panel compact">
              <p className="section-eyebrow">Trip Detail</p>
              <h3>Select a saved itinerary</h3>
              <p>Open any itinerary from the list to inspect the saved destination, dates, and generated plan.</p>
            </div>
          ) : null}

          {selectedItinerary ? (
            <article className="trip-detail-card">
              <p className="section-eyebrow">{selectedItinerary.destination}</p>
              <h3>{selectedItinerary.title}</h3>
              <div className="trip-meta">
                <span>
                  Dates: {selectedItinerary.startDate || 'TBD'} - {selectedItinerary.endDate || 'TBD'}
                </span>
              </div>
              <div className="trip-copy">
                {selectedItinerary.itineraryContent.split('\n').map((line, index) => (
                  <p key={`${selectedItinerary.id}-${index}`}>{line || '\u00a0'}</p>
                ))}
              </div>
            </article>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
