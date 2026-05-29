import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Trash2, Plus } from 'lucide-react';
import { snippetApi } from '../api/snippetApi';
import { extractErrorMessage } from '../api/axiosClient';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';

export default function Snippets() {
  const [page, setPage] = useState(null);
  const [pageNum, setPageNum] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ title: '', language: 'PYTHON', code: '' });

  const load = async (p = pageNum) => {
    setLoading(true); setError('');
    try {
      const data = await snippetApi.list({ page: p, size: 10 });
      setPage(data); setPageNum(p);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(0); /* eslint-disable-next-line */ }, []);

  const handleDelete = async (id) => {
    if (!window.confirm(`Delete snippet #${id}?`)) return;
    try {
      await snippetApi.delete(id);
      load(pageNum);
    } catch (err) { setError(extractErrorMessage(err)); }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await snippetApi.create(form);
      setForm({ title: '', language: 'PYTHON', code: '' });
      setCreating(false);
      load(0);
    } catch (err) { setError(extractErrorMessage(err)); }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Snippets</h1>
          <p className="page-subtitle">Your saved code snippets.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setCreating((s) => !s)}>
          <Plus size={16} /> {creating ? 'Cancel' : 'New Snippet'}
        </button>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      {creating && (
        <div className="card">
          <h3>Create snippet</h3>
          <form onSubmit={handleCreate}>
            <div className="form-field">
              <label>Title</label>
              <input required maxLength={200} value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="form-field">
              <label>Language</label>
              <select value={form.language} onChange={(e) => setForm({ ...form, language: e.target.value })}>
                <option value="PYTHON">Python</option>
                <option value="JAVA">Java</option>
                <option value="JAVASCRIPT">JavaScript</option>
              </select>
            </div>
            <div className="form-field">
              <label>Code</label>
              <textarea rows={10} required value={form.code}
                onChange={(e) => setForm({ ...form, code: e.target.value })} />
            </div>
            <div className="form-actions">
              <button className="btn btn-primary" type="submit">Create</button>
            </div>
          </form>
        </div>
      )}

      <div className="card">
        {loading ? <Loading /> : (
          <>
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th><th>Title</th><th>Language</th><th>Created</th><th>Updated</th><th></th>
                </tr>
              </thead>
              <tbody>
                {page?.content?.length ? page.content.map((s) => (
                  <tr key={s.id}>
                    <td className="mono">#{s.id}</td>
                    <td><Link to={`/snippets/${s.id}`}>{s.title}</Link></td>
                    <td><span className="badge badge-neutral">{s.language}</span></td>
                    <td className="mono">{new Date(s.createdAt).toLocaleString()}</td>
                    <td className="mono">{new Date(s.updatedAt).toLocaleString()}</td>
                    <td>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(s.id)}>
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                )) : (
                  <tr><td colSpan={6} className="muted" style={{ textAlign: 'center', padding: 30 }}>
                    No snippets yet. Use the editor or the New Snippet button.
                  </td></tr>
                )}
              </tbody>
            </table>
            <Pagination page={page} onChange={load} />
          </>
        )}
      </div>
    </div>
  );
}

function Pagination({ page, onChange }) {
  if (!page) return null;
  const totalPages = page.totalPages ?? 0;
  const current = page.number ?? 0;
  return (
    <div className="pagination">
      <span className="muted">Page {current + 1} of {Math.max(totalPages, 1)}</span>
      <button className="btn btn-ghost btn-sm" disabled={current <= 0}
              onClick={() => onChange(current - 1)}>Prev</button>
      <button className="btn btn-ghost btn-sm" disabled={current + 1 >= totalPages}
              onClick={() => onChange(current + 1)}>Next</button>
    </div>
  );
}
