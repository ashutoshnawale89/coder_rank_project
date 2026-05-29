import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { submissionApi } from '../api/submissionApi';
import { extractErrorMessage } from '../api/axiosClient';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import OutputPanel from '../components/OutputPanel';

export default function SubmissionDetail() {
  const { id } = useParams();
  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    submissionApi.get(id)
      .then((data) => active && setSubmission(data))
      .catch((err) => active && setError(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [id]);

  if (loading) return <div className="page"><Loading /></div>;
  if (!submission) return <div className="page"><ErrorMessage message={error || 'Not found'} /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <Link to="/submissions" className="muted"><ArrowLeft size={14} /> Back to submissions</Link>
          <h1>Submission #{submission.id}</h1>
          <p className="page-subtitle mono">{new Date(submission.createdAt).toLocaleString()}</p>
        </div>
      </div>

      <div className="card">
        <OutputPanel result={submission} />
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Code</h3>
        <pre className="output-block">{submission.code}</pre>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Stdin</h3>
        <pre className="output-block">{submission.stdin || <em>(empty)</em>}</pre>
      </div>
    </div>
  );
}
