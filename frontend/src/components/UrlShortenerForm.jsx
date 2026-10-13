import { useState } from 'react';
import ResultCard from './ResultCard';
import '../styles/UrlShortener.css';

const UrlShortenerForm = () => {
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [shortenedUrl, setShortenedUrl] = useState('');

  const validateUrl = (string) => {
    try {
      new URL(string);
      return true;
    } catch {
      return false;
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setShortenedUrl('');

    if (!url.trim()) {
      setError('Please enter a URL');
      return;
    }

    if (!validateUrl(url)) {
      setError('Please enter a valid URL (e.g., https://example.com)');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch('/api/shorten', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ url: url }),
      });

      if (!response.ok) {
        throw new Error('Failed to shorten URL');
      }

      const data = await response.json();
      setShortenedUrl(data.shortUrl);
    } catch {
      setError('An error occurred while shortening the URL. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="glass-card">
      <form onSubmit={handleSubmit} className="url-form">
        <div className="input-group">
          <input
            type="url"
            value={url}
            onChange={(e) => {
              setUrl(e.target.value);
              if (error) setError('');
            }}
            placeholder="Paste your long URL here..."
            className="url-input"
            disabled={isLoading}
            required
          />
          {error && <span className="error-message">{error}</span>}
        </div>
        
        <button type="submit" className="submit-btn" disabled={isLoading}>
          {isLoading ? (
            <>
              <span className="loader"></span>
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
    </div>
  );
};

export default UrlShortenerForm;
