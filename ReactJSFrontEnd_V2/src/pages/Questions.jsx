import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { questionApi } from '../api/questionApi';
import { extractErrorMessage } from '../api/axiosClient';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';

const DIFF = {
  EASY:   'badge-success',
  MEDIUM: 'badge-warn',
  HARD:   'badge-error'
};

export default function Questions() {
  const [page, setPage] = useState(null);
  const [pageNum, setPageNum] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async (p = pageNum) => {
    setLoading(true); setError('');
    try {
      const data = await questionApi.list({ page: p, size: 10 });
      setPage(data); setPageNum(p);
    } catch (err) { setError(extractErrorMessage(err)); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(0); /* eslint-disable-next-line */ }, []);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Questions</h1>
          <p className="page-subtitle">Coding problems published by admins.</p>
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      <div className="card">
        {loading ? <Loading /> : (
          <>
            <table className="table">
              <thead>
                <tr><th>ID</th><th>Title</th><th>Difficulty</th><th>Created</th></tr>
              </thead>
              <tbody>
                {page?.content?.length ? page.content.map((q) => (
                  <tr key={q.id}>
                    <td className="mono">#{q.id}</td>
                    <td><Link to={`/questions/${q.id}`}>{q.title}</Link></td>
                    <td><span className={`badge ${DIFF[q.difficulty] || 'badge-neutral'}`}>{q.difficulty}</span></td>
                    <td className="mono">{new Date(q.createdAt).toLocaleString()}</td>
                  </tr>
                )) : (
                  <tr><td colSpan={4} className="muted" style={{ textAlign: 'center', padding: 30 }}>
                    No questions yet.
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
