import { useState } from 'react';
import '../styles/UrlShortener.css';

const ResultCard = ({ shortUrl }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(shortUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy text: ', err);
    }
  };

  return (
    <div className="result-card">
      <span className="result-label">Your shortened URL is ready:</span>
      <div className="result-url-group">
        <a href={shortUrl} target="_blank" rel="noopener noreferrer" className="short-url">
          {shortUrl}
        </a>
        <button 
          className={`copy-btn ${copied ? 'copied' : ''}`}
          onClick={handleCopy}
          aria-label="Copy to clipboard"
        >
          {copied ? 'Copied!' : 'Copy'}
        </button>
      </div>
    </div>
  );
};

export default ResultCard;
