import { useState } from 'react';
import '../styles/UrlShortener.css';

const API_BASE_URL = (
  import.meta.env.DEV ? '' : (import.meta.env.VITE_API_URL || '')
).replace(/\/+$/, '');

function AuthForm({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/${mode}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...(mode === 'register' ? { name: name.trim() } : {}),
          email: email.trim(),
          password,
        }),
      });
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Authentication failed.');
      }
      onAuthenticated(await response.json());
    } catch (requestError) {
      setError(requestError.message || 'Authentication failed.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <section className="auth-card glass-card">
      <div className="auth-icon">{mode === 'login' ? '↗' : '✦'}</div>
      <span className="section-kicker">{mode === 'login' ? 'WELCOME BACK' : 'GET STARTED'}</span>
      <h2>{mode === 'login' ? 'Sign in to Linkly' : 'Create your account'}</h2>
      <p className="card-description">
        {mode === 'login'
          ? 'Manage your links and see performance in one place.'
          : 'Save your links, track clicks, and share with confidence.'}
      </p>
      <form onSubmit={submit} className="url-form auth-form">
        {mode === 'register' && (
          <label className="field-label">Full name
            <input
              className="url-input"
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              maxLength="120"
              autoComplete="name"
              required
            />
          </label>
        )}
        <label className="field-label">Email address
          <input className="url-input" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        </label>
        <label className="field-label">Password <span>8+ characters</span>
          <input className="url-input" type="password" value={password} onChange={(event) => setPassword(event.target.value)} minLength="8" required />
        </label>
        {error && <span className="error-message" role="alert">{error}</span>}
        <button className="submit-btn" type="submit" disabled={isLoading}>
          {isLoading ? 'Please wait...' : mode === 'login' ? 'Sign in' : 'Create account'}
        </button>
      </form>
      <div className="auth-switch">
        <span>{mode === 'login' ? 'New to Linkly?' : 'Already using Linkly?'}</span>
        <button className="text-button" type="button" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
        {mode === 'login' ? 'Create an account' : 'Sign in instead'}
        </button>
      </div>
    </section>
  );
}

export default AuthForm;
