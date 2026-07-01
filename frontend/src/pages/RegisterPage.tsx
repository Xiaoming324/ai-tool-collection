import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../lib/api';

export function RegisterPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      await register({ username, password });
      navigate('/login', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create account.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-hero">
        <p className="section-eyebrow">First-time setup</p>
        <h1>Create an isolated workspace for your chats, documents, images, and itineraries.</h1>
        <p>
          The backend enforces per-user ownership for sessions, chat memory, stored files, PDF access,
          and saved travel itineraries.
        </p>
        <div className="auth-tags">
          <span>User isolation</span>
          <span>JWT-secured APIs</span>
          <span>Session deletion</span>
          <span>Signed file access</span>
        </div>
      </section>

      <section className="auth-panel">
        <div>
          <p className="section-eyebrow">New account</p>
          <h2>Register</h2>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Username
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="3-20 chars, visible ASCII only"
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="6-20 chars, visible ASCII only"
            />
          </label>

          {error ? <div className="form-error">{error}</div> : null}

          <button type="submit" className="primary-button wide" disabled={submitting}>
            {submitting ? 'Creating...' : 'Create account'}
          </button>
        </form>

        <p className="auth-foot">
          Already registered? <Link to="/login">Back to sign in</Link>
        </p>
      </section>
    </div>
  );
}
