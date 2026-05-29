const STATUS_COLORS = {
  SUCCESS: 'badge-success',
  COMPILE_ERROR: 'badge-error',
  RUNTIME_ERROR: 'badge-error',
  TIMEOUT: 'badge-warn',
  INTERNAL_ERROR: 'badge-error',
  ACCEPTED: 'badge-success',
  PARTIAL: 'badge-warn',
  WRONG_ANSWER: 'badge-error'
};

export default function OutputPanel({ result }) {
  if (!result) return null;
  const klass = STATUS_COLORS[result.status] || 'badge-neutral';
  return (
    <div className="output-panel">
      <div className="output-header">
        <span className={`badge ${klass}`}>{result.status}</span>
        {result.language && <span className="badge badge-neutral">{result.language}</span>}
        {typeof result.exitCode === 'number' && (
          <span className="badge badge-neutral">exit {result.exitCode}</span>
        )}
        {typeof result.executionTimeMs === 'number' && (
          <span className="badge badge-neutral">{result.executionTimeMs} ms</span>
        )}
        {result.submissionId && (
          <span className="badge badge-neutral">submission #{result.submissionId}</span>
        )}
      </div>
      <div className="output-grid">
        <section>
          <h4>stdout</h4>
          <pre className="output-block stdout">{result.stdout || <em>(empty)</em>}</pre>
        </section>
        <section>
          <h4>stderr</h4>
          <pre className="output-block stderr">{result.stderr || <em>(empty)</em>}</pre>
        </section>
      </div>
    </div>
  );
}
