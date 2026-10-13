import UrlShortenerForm from './components/UrlShortenerForm';
import './styles/UrlShortener.css';

function App() {
  return (
    <div className="app-container">
      <header className="hero">
        <h1 className="hero-title">
          Shorten <span>Any</span> Link.
        </h1>
        <p className="hero-subtitle">
          A premium, lightning-fast URL shortener built for modern web experiences. 
          Transform long, ugly links into clean, shareable URLs in seconds.
        </p>
      </header>

      <main style={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
        <UrlShortenerForm />
      </main>
    </div>
  );
}

export default App;
