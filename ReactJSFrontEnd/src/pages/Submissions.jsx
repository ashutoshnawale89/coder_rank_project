import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { submissionApi } from '../api/submissionApi';
import { extractErrorMessage } from '../api/axiosClient';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';

const STATUS_BADGE = {
  SUCCESS: 'badge-success',
  COMPILE_ERROR: 'badge-error',
  RUNTIME_ERROR: 'badge-error',
  TIMEOUT: 'badge-warn',
  INTERNAL_ERROR: 'badge-error'
};

export default function Submissions() {
  const [page, setPage] = useState(null);
  const [pageNum, setPageNum] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async (p = pageNum) => {
    setLoading(true); setError('');
    try {
      const data = await submissionApi.list({ page: p, size: 15 });
      setPage(data); setPageNum(p);
    } catch (err) { setError(extractErrorMessage(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(0); /* eslint-disable-next-line */ }, []);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Submissions</h1>
          <p className="page-subtitle">Every code execution you've made.</p>
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      <div className="card">
        {loading ? <Loading /> : (
          <>
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th><th>Language</th><th>Status</th><th>Exit</th><th>Time (ms)</th><th>Created</th>
                </tr>
              </thead>
              <tbody>
                {page?.content?.length ? page.content.map((s) => (
                  <tr key={s.id}>
                    <td className="mono"><Link to={`/submissions/${s.id}`}>#{s.id}</Link></td>
                    <td><span className="badge badge-neutral">{s.language}</span></td>
                    <td><span className={`badge ${STATUS_BADGE[s.status] || 'badge-neutral'}`}>{s.status}</span></td>
                    <td className="mono">{s.exitCode}</td>
                    <td className="mono">{s.executionTimeMs}</td>
                    <td className="mono">{new Date(s.createdAt).toLocaleString()}</td>
                  </tr>
                )) : (
                  <tr><td colSpan={6} className="muted" style={{ textAlign: 'center', padding: 30 }}>
                    No submissions yet. Run some code in the editor.
                  </td></tr>
                )}
              </tbody>
            </table>
            <div className="pagination">
              <span className="muted">Page {(page?.number ?? 0) + 1} of {Math.max(page?.totalPages ?? 0, 1)}</span>
              <button className="btn btn-ghost btn-sm" disabled={(page?.number ?? 0) <= 0}
                onClick={() => load((page?.number ?? 0) - 1)}>Prev</button>
              <button className="btn btn-ghost btn-sm"
                disabled={((page?.number ?? 0) + 1) >= (page?.totalPages ?? 0)}
                onClick={() => load((page?.number ?? 0) + 1)}>Next</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
