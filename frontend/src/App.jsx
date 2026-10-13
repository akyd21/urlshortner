import UrlShortenerForm from './components/UrlShortenerForm';
import AuthForm from './components/AuthForm';
import './styles/UrlShortener.css';
import { useState } from 'react';

function App() {
  const [session, setSession] = useState(() => {
    const token = localStorage.getItem('linkly_token');
    const email = localStorage.getItem('linkly_email');
    return token && email ? { token, email } : null;
  });

  const authenticate = ({ token, email }) => {
    localStorage.setItem('linkly_token', token);
    localStorage.setItem('linkly_email', email);
    setSession({ token, email });
  };

  const logout = () => {
    localStorage.removeItem('linkly_token');
    localStorage.removeItem('linkly_email');
    setSession(null);
  };

  return (
    <div className="app-container">
      <nav className="topbar">
        <a className="brand" href="/">
          <span className="brand-mark">↗</span>
          <span>Linkly</span>
        </a>
        {session ? (
          <div className="account-actions">
            <button className="profile-button" onClick={() => document.getElementById('link-history')?.scrollIntoView({ behavior: 'smooth' })}>My links</button>
            <span className="account-email">{session.email}</span>
            <button className="logout-button" onClick={logout}>Log out</button>
          </div>
        ) : <span className="status-pill"><span className="status-dot" /> Fast & secure</span>}
      </nav>

      <header className="hero">
        <div className="eyebrow">LINK MANAGEMENT, SIMPLIFIED</div>
        <h1 className="hero-title">Make every link<br /><span>worth sharing.</span></h1>
        <p className="hero-subtitle">
          Turn long URLs into clean, memorable links. Add your own alias,
          set an expiry, and track every click.
        </p>
      </header>

      <main className="workspace">
        {session ? <UrlShortenerForm token={session.token} /> : <AuthForm onAuthenticated={authenticate} />}
      </main>

      <section className="feature-strip" aria-label="Features">
        <div><span className="feature-icon">✦</span><strong>Instant links</strong><small>Ready in seconds</small></div>
        <div><span className="feature-icon">◈</span><strong>Smart controls</strong><small>Aliases & expiry dates</small></div>
        <div><span className="feature-icon">↗</span><strong>Built to share</strong><small>Analytics & QR codes</small></div>
      </section>

      <footer className="footer">Simple links for a faster web <span>•</span> Secure account dashboard</footer>
    </div>
  );
}

export default App;
