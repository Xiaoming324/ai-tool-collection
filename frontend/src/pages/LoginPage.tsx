import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { login } from '../lib/api';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { signIn } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const token = await login({ username, password });
      signIn(token, username);
      const nextPath = (location.state as { from?: string } | null)?.from ?? '/app/chat';
      navigate(nextPath, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to sign in.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-hero">
        <p className="section-eyebrow">AI Tool Collection</p>
        <h1>One workspace for multimodal chat, ChatPDF, and travel planning.</h1>
        <p>
          Sign in to access persistent chat sessions, signed S3 attachments, PDF retrieval workflows,
          and tool-driven itinerary planning from a single React frontend.
        </p>
        <div className="auth-tags">
          <span>JWT authentication</span>
          <span>MySQL session history</span>
          <span>S3-backed files</span>
          <span>Redis RAG</span>
        </div>
      </section>

      <section className="auth-panel">
        <div>
          <p className="section-eyebrow">Welcome back</p>
          <h2>Sign in</h2>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Username
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="3-20 chars, no spaces"
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="6-20 chars, no spaces"
            />
          </label>

          {error ? <div className="form-error">{error}</div> : null}

          <button type="submit" className="primary-button wide" disabled={submitting}>
            {submitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="auth-foot">
          Need an account? <Link to="/register">Create one</Link>
        </p>
      </section>
    </div>
  );
}
