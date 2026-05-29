import { useEffect, useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { healthApi } from '../api/healthApi';
import { extractErrorMessage } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage';

export default function Health() {
  const [status, setStatus] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [checkedAt, setCheckedAt] = useState(null);

  const check = async () => {
    setLoading(true); setError('');
    try {
      const data = await healthApi.check();
      setStatus(data?.status || 'UP');
      setCheckedAt(new Date());
    } catch (err) {
      setStatus('DOWN');
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { check(); }, []);

  const klass = status === 'UP' ? 'badge-success' : status === 'DOWN' ? 'badge-error' : 'badge-neutral';

  return (
    <div className="page page-narrow">
      <div className="page-header">
        <div>
          <h1>API Health</h1>
          <p className="page-subtitle">Backend liveness probe.</p>
        </div>
        <button className="btn btn-ghost" onClick={check} disabled={loading}>
          <RefreshCw size={16} /> {loading ? 'Checking…' : 'Recheck'}
        </button>
      </div>

      <ErrorMessage message={error} />

      <div className="card">
        <div className="row spread">
          <div>
            <div className="muted">Status</div>
            <div style={{ marginTop: 6 }}><span className={`badge ${klass}`}>{status ?? '—'}</span></div>
          </div>
          {checkedAt && <div className="muted mono">Last check: {checkedAt.toLocaleTimeString()}</div>}
        </div>
      </div>
    </div>
  );
}
