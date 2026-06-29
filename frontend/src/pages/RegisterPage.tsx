import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../lib/api';

export function RegisterPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      await register({ username, password });
      navigate('/login', {
        replace: true,
        state: { registered: true },
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create the account.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-scene">
      <section className="auth-copy">
        <p className="eyebrow">First-time setup</p>
        <h1>Create an isolated workspace for your sessions, files, and future PDFs.</h1>
        <p>
          Every user is separated by JWT-authenticated history, chat memory, and S3-backed file ownership.
        </p>
        <div className="auth-badges">
          <span>User isolation</span>
          <span>JWT auth</span>
          <span>MySQL session history</span>
        </div>
      </section>

      <section className="auth-card">
        <div>
          <p className="eyebrow">New account</p>
          <h2>Register</h2>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Username
            <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="No spaces" />
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

          <button type="submit" className="primary-button" disabled={isSubmitting}>
            {isSubmitting ? 'Creating...' : 'Create account'}
          </button>
        </form>

        <p className="auth-foot">
          Already registered? <Link to="/login">Back to sign in</Link>
        </p>
      </section>
    </div>
  );
}
