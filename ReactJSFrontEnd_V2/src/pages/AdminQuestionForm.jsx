import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Trash2 } from 'lucide-react';
import { adminQuestionApi } from '../api/questionApi';
import { extractErrorMessage } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage';

function emptyTestCase(sample) {
  return { input: '', expectedOutput: '', sample };
}

export default function AdminQuestionForm() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '', description: '', difficulty: 'EASY',
    testCases: [emptyTestCase(true), emptyTestCase(true), emptyTestCase(false)]
  });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const updateTC = (i, patch) => {
    const next = [...form.testCases];
    next[i] = { ...next[i], ...patch };
    setForm({ ...form, testCases: next });
  };

  const addTC = (sample) => {
    setForm({ ...form, testCases: [...form.testCases, emptyTestCase(sample)] });
  };

  const removeTC = (i) => {
    const next = form.testCases.filter((_, idx) => idx !== i);
    setForm({ ...form, testCases: next });
  };

  const sampleCount = form.testCases.filter((t) => t.sample).length;
  const hiddenCount = form.testCases.length - sampleCount;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSaving(true);
    try {
      const data = await adminQuestionApi.create([form]);
      const created = Array.isArray(data) ? data[0] : data;
      navigate(`/questions/${created.id}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>New Question</h1>
          <p className="page-subtitle">Admin only. Up to 5 sample + 100 hidden test cases.</p>
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      <form onSubmit={handleSubmit}>
        <div className="card">
          <div className="form-field">
            <label>Title</label>
            <input required maxLength={200}
              value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
          </div>
          <div className="form-field">
            <label>Difficulty</label>
            <select value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: e.target.value })}>
              <option value="EASY">EASY</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HARD">HARD</option>
            </select>
          </div>
          <div className="form-field">
            <label>Description</label>
            <textarea required rows={6} maxLength={50_000}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
        </div>

        <div className="card">
          <div className="row spread">
            <h3 style={{ margin: 0 }}>Test cases ({sampleCount} sample / {hiddenCount} hidden)</h3>
            <div className="row">
              <button type="button" className="btn btn-ghost" onClick={() => addTC(true)}
                disabled={sampleCount >= 5}>
                <Plus size={14} /> Add sample
              </button>
              <button type="button" className="btn btn-ghost" onClick={() => addTC(false)}
                disabled={hiddenCount >= 100}>
                <Plus size={14} /> Add hidden
              </button>
            </div>
          </div>
          <div className="tc-list" style={{ marginTop: 12 }}>
            {form.testCases.map((tc, i) => (
              <div className="tc-item" key={i}>
                <div className="row spread" style={{ marginBottom: 8 }}>
                  <h5 style={{ margin: 0 }}>
                    <span className="badge badge-neutral">#{i}</span>
                    <span className={`badge ${tc.sample ? 'badge-success' : 'badge-neutral'}`}>
                      {tc.sample ? 'Sample' : 'Hidden'}
                    </span>
                  </h5>
                  <button type="button" className="btn btn-danger btn-sm" onClick={() => removeTC(i)}>
                    <Trash2 size={14} />
                  </button>
                </div>
                <div className="output-grid">
                  <div className="form-field" style={{ margin: 0 }}>
                    <label>Input (stdin)</label>
                    <textarea rows={4} maxLength={50_000}
                      value={tc.input}
                      onChange={(e) => updateTC(i, { input: e.target.value })} />
                  </div>
                  <div className="form-field" style={{ margin: 0 }}>
                    <label>Expected output</label>
                    <textarea rows={4} required maxLength={50_000}
                      value={tc.expectedOutput}
                      onChange={(e) => updateTC(i, { expectedOutput: e.target.value })} />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="form-actions">
          <button className="btn btn-primary" type="submit" disabled={saving || !form.testCases.length}>
            {saving ? 'Creating…' : 'Create question'}
          </button>
        </div>
      </form>
    </div>
  );
}
