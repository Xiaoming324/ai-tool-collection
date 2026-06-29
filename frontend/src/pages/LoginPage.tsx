import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login } from '../lib/api';
import { useAuth } from '../contexts/AuthContext';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { signIn } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const token = await login({ username, password });
      signIn(token, username);
      const nextPath = (location.state as { from?: string } | null)?.from ?? '/app/chat';
      navigate(nextPath, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to sign in.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-scene">
      <section className="auth-copy">
        <p className="eyebrow">AI Tool Collection</p>
        <h1>Run module 2 and module 4 from one focused workspace.</h1>
        <p>
          This frontend is wired for Claude streaming chat, multimodal image uploads, persistent session history,
          and the planned travel assistant tool-calling flow.
        </p>
        <div className="auth-badges">
          <span>Persistent memory</span>
          <span>Signed image URLs</span>
          <span>Travel tool surface</span>
        </div>
      </section>

      <section className="auth-card">
        <div>
          <p className="eyebrow">Welcome back</p>
          <h2>Sign in</h2>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Username
            <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="3-20 chars" />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="6-20 chars"
            />
          </label>

          {error ? <div className="form-error">{error}</div> : null}

          <button type="submit" className="primary-button" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="auth-foot">
          Need an account? <Link to="/register">Create one</Link>
        </p>
      </section>
    </div>
  );
}
