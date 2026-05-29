import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Play, Trash2 } from 'lucide-react';
import { snippetApi } from '../api/snippetApi';
import { executionApi } from '../api/executionApi';
import { extractErrorMessage } from '../api/axiosClient';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import OutputPanel from '../components/OutputPanel';

export default function SnippetDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [snippet, setSnippet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [stdin, setStdin] = useState('');
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    snippetApi.get(id)
      .then((data) => { if (active) setSnippet(data); })
      .catch((err) => active && setError(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [id]);

  const handleRun = async () => {
    setRunning(true); setResult(null); setError('');
    try {
      const data = await executionApi.execute({
        language: snippet.language,
        code: snippet.code,
        stdin,
        snippetId: snippet.id
      });
      setResult(data);
    } catch (err) { setError(extractErrorMessage(err)); }
    finally { setRunning(false); }
  };

  const handleDelete = async () => {
    if (!window.confirm(`Delete snippet #${id}?`)) return;
    try { await snippetApi.delete(id); navigate('/snippets'); }
    catch (err) { setError(extractErrorMessage(err)); }
  };

  if (loading) return <div className="page"><Loading /></div>;
  if (!snippet) return <div className="page"><ErrorMessage message={error || 'Not found'} /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <Link to="/snippets" className="muted"><ArrowLeft size={14} /> Back to snippets</Link>
          <h1>{snippet.title}</h1>
          <p className="page-subtitle">
            <span className="badge badge-neutral">{snippet.language}</span>{' '}
            <span className="mono">#{snippet.id}</span>
          </p>
        </div>
        <div className="row">
          <button className="btn btn-primary" onClick={handleRun} disabled={running}>
            <Play size={16} /> {running ? 'Running…' : 'Run'}
          </button>
          <button className="btn btn-danger" onClick={handleDelete}>
            <Trash2 size={16} /> Delete
          </button>
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Code</h3>
        <pre className="output-block">{snippet.code}</pre>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Standard input (stdin)</h3>
        <textarea rows={4} value={stdin} onChange={(e) => setStdin(e.target.value)}
          placeholder="(optional)" />
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Output</h3>
        {!result && !running && <p className="muted">Click Run to execute this snippet.</p>}
        {running && <p className="muted">Executing in sandbox…</p>}
        <OutputPanel result={result} />
      </div>
    </div>
  );
}
