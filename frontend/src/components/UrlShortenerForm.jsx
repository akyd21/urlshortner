
import { useState } from 'react';
import ResultCard from './ResultCard';
import '../styles/UrlShortener.css';

// Configure the backend URL.
// Local development uses the Vite proxy or relative URL.
// Production uses VITE_API_URL from frontend/.env.
const API_BASE_URL = (
  import.meta.env.DEV
    ? ''
    : (import.meta.env.VITE_API_URL || '')
).replace(/\/+$/, '');

const UrlShortenerForm = () => {
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [shortenedUrl, setShortenedUrl] = useState('');

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
          },
          body: JSON.stringify({ url: trimmedUrl }),
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

  return (
    <div className="glass-card">
      <form onSubmit={handleSubmit} className="url-form">
        <div className="input-group">
          <input
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
    </div>
  );
};

export default UrlShortenerForm;
