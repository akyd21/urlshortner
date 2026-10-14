
import { useCallback, useEffect, useState } from 'react';
import ResultCard from './ResultCard';
import '../styles/UrlShortener.css';

const API_BASE_URL = (
  import.meta.env.DEV
    ? ''
    : (import.meta.env.VITE_API_URL || '')
).replace(/\/+$/, '');

const UrlShortenerForm = ({ token }) => {
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [shortenedUrl, setShortenedUrl] = useState('');
  const [alias, setAlias] = useState('');
  const [expiresAt, setExpiresAt] = useState('');
  const [bulkUrls, setBulkUrls] = useState('');
  const [bulkResults, setBulkResults] = useState([]);
  const [stats, setStats] = useState(null);
  const [links, setLinks] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [selectedLink, setSelectedLink] = useState('');

  const loadLinks = useCallback(async () => {
    setHistoryLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/links`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) {
        throw new Error('Could not load your link history.');
      }
      setLinks(await response.json());
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setHistoryLoading(false);
    }
  }, [token]);

  const loadStats = async (shortCode) => {
    const response = await fetch(`${API_BASE_URL}/api/links/${shortCode}/stats`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!response.ok) {
      throw new Error('Could not load analytics for this link.');
    }
    const latestStats = await response.json();
    setSelectedLink(shortCode);
    setStats(latestStats);
    await loadLinks();
  };

  const deleteLink = async (link) => {
    if (!window.confirm(`Delete ${link.shortUrl}? This action cannot be undone.`)) {
      return;
    }

    setError('');
    try {
      const response = await fetch(`${API_BASE_URL}/api/links/${encodeURIComponent(link.shortCode)}`, {
        method: 'DELETE',
        headers: { Authorization: 'Bearer ' + token },
      });
      if (!response.ok) {
        const responseBody = await response.json().catch(() => ({}));
        throw new Error(responseBody.message || `Request failed with status ${response.status}`);
      }
      setLinks((currentLinks) => currentLinks.filter((currentLink) => currentLink.shortCode !== link.shortCode));
      if (selectedLink === link.shortCode) {
        setSelectedLink('');
        setStats(null);
      }
    } catch (requestError) {
      setError(requestError.message || 'Could not delete this link.');
    }
  };

  useEffect(() => {
    loadLinks();
  }, [loadLinks]);

  useEffect(() => {
    window.addEventListener('focus', loadLinks);
    return () => window.removeEventListener('focus', loadLinks);
  }, [loadLinks]);

  const validateUrl = (value) => {
    try {
      const parsedUrl = new URL(value);
      return (
        parsedUrl.protocol === 'http:' ||
        parsedUrl.protocol === 'https:'
      );
    } catch {
      return false;
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError('');
    setShortenedUrl('');
    setBulkResults([]);

    const trimmedUrl = url.trim();

    if (!trimmedUrl) {
      setError('Please enter a URL.');
      return;
    }

    if (!validateUrl(trimmedUrl)) {
      setError('Please enter a valid URL starting with https:// or http://.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/shorten`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            url: trimmedUrl,
            alias: alias.trim() || null,
            expiresAt: expiresAt ? (expiresAt.includes("T") ? expiresAt : (expiresAt + "T23:59:59")) : null,
          }),
        }
      );

      if (!response.ok) {
        const responseText = await response.text();
        console.error(
          'URL shortening failed:',
          response.status,
          responseText
        );

        throw new Error(
          `Request failed with status ${response.status}`
        );
      }

      const data = await response.json();

      if (!data.shortUrl) {
        console.error('Unexpected backend response:', data);
        throw new Error('The backend response did not contain shortUrl.');
      }

      setShortenedUrl(data.shortUrl);
      await loadLinks();
    } catch (err) {
      console.error('Error shortening URL:', err);

      if (err instanceof TypeError) {
        setError(
          'Cannot connect to the server. Please check the backend URL and CORS configuration.'
        );
      } else {
        setError(
          err.message || 'An error occurred while shortening the URL.'
        );
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleBulkSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setBulkResults([]);
    const urls = bulkUrls.split(/\r?\n/).map((value) => value.trim()).filter(Boolean);
    if (!urls.length) {
      setError('Enter at least one URL for bulk shortening.');
      return;
    }
    setIsLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/shorten/bulk`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify(urls.map((value) => ({ url: value }))),
      });
      if (!response.ok) throw new Error(`Request failed with status ${response.status}`);
      setBulkResults(await response.json());
      await loadLinks();
    } catch (err) {
      setError(err.message || 'Bulk shortening failed.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="dashboard-grid">
      <section className="glass-card quick-card">
        <div className="card-heading">
          <div>
            <span className="section-kicker">QUICK SHORTEN</span>
            <h2>Create a short link</h2>
          </div>
          <span className="card-spark">✦</span>
        </div>
      <form onSubmit={handleSubmit} className="url-form">
        <div className="input-group">
          <label htmlFor="long-url">Long URL</label>
          <input
            id="long-url"
            type="url"
            value={url}
            onChange={(event) => {
              setUrl(event.target.value);
              if (error) setError('');
            }}
            placeholder="Paste your long URL here..."
            className="url-input"
            disabled={isLoading}
            required
          />

          {error && (
            <span className="error-message" role="alert">
              {error}
            </span>
          )}
        </div>

        <div className="advanced-grid">
          <label className="field-label">
            Custom alias <span>optional</span>
            <input
              value={alias}
              onChange={(event) => setAlias(event.target.value)}
              placeholder="e.g. summer-sale"
              className="url-input"
              maxLength="32"
              disabled={isLoading}
            />
          </label>
          <label className="field-label">
            Expire link <span>default: 3 months</span>
          <input
            type="date"
            value={expiresAt}
            onChange={(event) => setExpiresAt(event.target.value)}
            className="url-input"
            disabled={isLoading}
          />
          </label>
        </div>

        <button
          type="submit"
          className="submit-btn"
          disabled={isLoading}
        >
          {isLoading ? (
            <>
              <span className="loader" />
              Shortening...
            </>
          ) : (
            'Shorten URL'
          )}
        </button>
      </form>

      {shortenedUrl && !isLoading && (
        <ResultCard shortUrl={shortenedUrl} />
      )}

      {shortenedUrl && (
        <div className="feature-actions">
          <button type="button" onClick={async () => {
            try {
              await loadStats(shortenedUrl.split('/').pop());
            } catch (requestError) {
              setError(requestError.message);
            }
          }}>↗ View analytics</button>
        </div>
      )}
      </section>

      <form onSubmit={handleBulkSubmit} className="glass-card bulk-form">
        <div className="card-heading">
          <div>
            <span className="section-kicker">BULK MODE</span>
            <h2>Shorten multiple links</h2>
          </div>
          <span className="card-spark">⌁</span>
        </div>
        <p className="card-description">Paste one URL per line and get a clean list of shareable links.</p>
        <textarea
          value={bulkUrls}
          onChange={(event) => setBulkUrls(event.target.value)}
          placeholder="Paste one URL per line"
          rows="4"
          disabled={isLoading}
        />
        <button type="submit" className="submit-btn secondary-btn" disabled={isLoading}>Shorten all URLs <span>→</span></button>
      </form>
      {bulkResults.length > 0 && (
        <div className="bulk-results glass-card">
          <span className="section-kicker">YOUR LINKS</span>
          {bulkResults.map((result) => (
            <a key={result.shortCode} href={result.shortUrl} target="_blank" rel="noreferrer">
              {result.shortUrl}
            </a>
          ))}
        </div>
      )}
      <section className="glass-card link-history" id="link-history">
        <div className="card-heading">
          <div>
            <span className="section-kicker">YOUR PROFILE</span>
            <h2>All your links</h2>
          </div>
          <span className="history-count">{links.length} total</span>
        </div>
        <p className="card-description">Manage the links you have created.</p>
        {historyLoading ? (
          <p className="history-empty">Loading your link history...</p>
        ) : links.length === 0 ? (
          <p className="history-empty">Your generated links will appear here.</p>
        ) : (
          <div className="link-history-list">
            {links.map((link) => (
              <article className="history-item" key={link.shortCode}>
                <div className="history-main">
                  <a className="history-short-url" href={link.shortUrl} target="_blank" rel="noreferrer">
                    {link.shortUrl}
                  </a>
                  <span className="history-original" title={link.originalUrl}>{link.originalUrl}</span>
                  <span className="history-meta">
                    Created {new Date(link.createdAt).toLocaleDateString()} · {link.clickCount} clicks · {link.domain}
                  </span>
                  <div className="history-tags">
                    <span className={`status-badge status-${(link.status || 'ACTIVE').toLowerCase()}`}>
                      {link.status || 'ACTIVE'}
                    </span>
                    {link.alias && <span className="alias-badge">/{link.alias}</span>}
                  </div>
                </div>
                <div className="history-actions">
                  <button
                    type="button"
                    className="history-analytics-button"
                    onClick={async () => {
                      try {
                        await loadStats(link.shortCode);
                      } catch (requestError) {
                        setError(requestError.message);
                      }
                    }}
                  >
                    View analytics
                  </button>
                  <button
                    type="button"
                    className="history-delete-button"
                    onClick={() => deleteLink(link)}
                  >
                    Delete
                  </button>
                </div>
                {selectedLink === link.shortCode && stats && (
                  <div className="history-stats">
                    <strong>{stats.clickCount} clicks</strong>
                    <span>Status: {link.status || stats.status}</span>
                    <span>Domain: {link.domain || stats.domain}</span>
                    <span>Last clicked: {link.lastClickedAt ? new Date(link.lastClickedAt).toLocaleString() : 'Not clicked yet'}</span>
                    <span>Created: {new Date(stats.createdAt).toLocaleString()}</span>
                    {stats.expiresAt && <span>Expires: {new Date(stats.expiresAt).toLocaleString()}</span>}
                  </div>
                )}
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
};

export default UrlShortenerForm;
