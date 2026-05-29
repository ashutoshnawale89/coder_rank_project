import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="page page-narrow">
      <div className="card" style={{ textAlign: 'center' }}>
        <h1 style={{ fontSize: 64, margin: 0 }}>404</h1>
        <p className="muted">This page doesn't exist.</p>
        <Link to="/" className="btn btn-primary">Go home</Link>
      </div>
    </div>
  );
}
