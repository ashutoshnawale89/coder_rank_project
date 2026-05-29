import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { ArrowLeft, Play, Trash2, FileText, FlaskConical, Terminal, CheckCircle2, XCircle } from 'lucide-react';
import { questionApi, adminQuestionApi } from '../api/questionApi';
import { extractErrorMessage } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';

const STATUS_BADGE = {
  ACCEPTED: 'badge-success',
  PARTIAL: 'badge-warn',
  WRONG_ANSWER: 'badge-error',
  COMPILE_ERROR: 'badge-error',
  RUNTIME_ERROR: 'badge-error',
  TIMEOUT: 'badge-warn',
  INTERNAL_ERROR: 'badge-error'
};

const DIFFICULTY_BADGE = {
  EASY: 'badge-success',
  MEDIUM: 'badge-warn',
  HARD: 'badge-error'
};

const LANG = { PYTHON: 'python', JAVA: 'java', JAVASCRIPT: 'javascript' };
const SAMPLE_BY_LANG = {
  PYTHON:
    '# Read stdin -> print to stdout.\n' +
    '# int:    n = int(input())\n' +
    '# string: s = input()\n',

  JAVA:
    '// Read stdin -> print to stdout.\n' +
    '// int:    int n = sc.nextInt();\n' +
    '// string: String s = sc.nextLine();\n' +
    'import java.util.Scanner;\n' +
    '\n' +
    'public class Main {\n' +
    '    public static void main(String[] args) {\n' +
    '        Scanner sc = new Scanner(System.in);\n' +
    '    }\n' +
    '}\n',

  JAVASCRIPT:
    '// Read stdin -> print to stdout.\n' +
    'let data = "";\n' +
    'process.stdin.on("data", chunk => data += chunk);\n' +
    'process.stdin.on("end", () => {\n' +
    '    const input = data.trim();\n' +
    '    // int:    const n = parseInt(input, 10);\n' +
    '    // string: const s = input;\n' +
    '});\n'
};

export default function QuestionDetail() {
  const { id } = useParams();
  const { isAdmin } = useAuth();
  const [question, setQuestion] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [language, setLanguage] = useState('PYTHON');
  const [code, setCode] = useState(SAMPLE_BY_LANG.PYTHON);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);
  const [leftTab, setLeftTab] = useState('description');
  const [bottomTab, setBottomTab] = useState('testcases');

  useEffect(() => {
    let active = true;
    questionApi.get(id)
      .then((data) => active && setQuestion(data))
      .catch((err) => active && setError(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [id]);

  const handleLanguageChange = (e) => {
    const v = e.target.value;
    setLanguage(v);
    setCode(SAMPLE_BY_LANG[v]);
  };

  const handleSubmit = async () => {
    setSubmitting(true); setError(''); setResult(null);
    setBottomTab('result');
    try {
      const data = await questionApi.solve(id, { language, code });
      setResult(data);
    } catch (err) { setError(extractErrorMessage(err)); }
    finally { setSubmitting(false); }
  };

  const handleDelete = async () => {
    if (!window.confirm(`Delete question #${id}? This cannot be undone.`)) return;
    try { await adminQuestionApi.delete(id); window.location.assign('/questions'); }
    catch (err) { setError(extractErrorMessage(err)); }
  };

  if (loading) return <div className="page"><Loading /></div>;
  if (!question) return <div className="page"><ErrorMessage message={error || 'Not found'} /></div>;

  const difficultyClass = DIFFICULTY_BADGE[question.difficulty] || 'badge-neutral';

  return (
    <div className="lc-shell">
      {/* Top bar */}
      <div className="lc-topbar">
        <Link to="/questions" className="lc-back">
          <ArrowLeft size={16} /> Problems
        </Link>
        <div className="lc-title-block">
          <h2 className="lc-title">{question.title}</h2>
          <span className={`badge ${difficultyClass}`}>{question.difficulty}</span>
          <span className="muted lc-meta">
            {question.sampleTestCaseCount} sample · {question.hiddenTestCaseCount} hidden
          </span>
        </div>
        <div className="lc-topbar-actions">
          {isAdmin && (
            <button className="btn btn-danger btn-sm" onClick={handleDelete}>
              <Trash2 size={14} /> Delete
            </button>
          )}
          <button
            className="btn btn-primary"
            onClick={handleSubmit}
            disabled={submitting || !code.trim()}
          >
            <Play size={16} /> {submitting ? 'Grading…' : 'Submit'}
          </button>
        </div>
      </div>

      {error && (
        <div style={{ padding: '8px 16px' }}>
          <ErrorMessage message={error} onDismiss={() => setError('')} />
        </div>
      )}

      {/* Split panes */}
      <div className="lc-split">
        {/* LEFT PANE — problem + sample test cases */}
        <div className="lc-pane lc-pane-left">
          <div className="lc-tabs">
            <button
              className={`lc-tab ${leftTab === 'description' ? 'is-active' : ''}`}
              onClick={() => setLeftTab('description')}
            >
              <FileText size={14} /> Description
            </button>
            <button
              className={`lc-tab ${leftTab === 'samples' ? 'is-active' : ''}`}
              onClick={() => setLeftTab('samples')}
            >
              <FlaskConical size={14} /> Sample Tests
              <span className="lc-tab-count">{question.sampleTestCases?.length || 0}</span>
            </button>
          </div>

          <div className="lc-pane-body">
            {leftTab === 'description' && (
              <div className="lc-description">
                <h1 className="lc-problem-title">{question.title}</h1>
                <div className="lc-problem-meta">
                  <span className={`badge ${difficultyClass}`}>{question.difficulty}</span>
                  <span className="muted">
                    {question.sampleTestCaseCount} sample · {question.hiddenTestCaseCount} hidden
                  </span>
                </div>
                <pre className="lc-prose">{question.description}</pre>

                {question.sampleTestCases?.length > 0 && (
                  <div className="lc-examples">
                    {question.sampleTestCases.slice(0, 3).map((tc, i) => (
                      <div className="lc-example" key={tc.id}>
                        <div className="lc-example-title">Example {i + 1}:</div>
                        <div className="lc-example-row">
                          <span className="lc-example-label">Input:</span>
                          <code className="lc-example-code">{tc.input || '(empty)'}</code>
                        </div>
                        <div className="lc-example-row">
                          <span className="lc-example-label">Output:</span>
                          <code className="lc-example-code">{tc.expectedOutput || '(empty)'}</code>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {leftTab === 'samples' && (
              <div className="lc-samples">
                {question.sampleTestCases?.length ? (
                  question.sampleTestCases.map((tc) => (
                    <div className="tc-item" key={tc.id}>
                      <h5>
                        <span className="badge badge-neutral">#{tc.orderIndex}</span>
                        {tc.sample ? 'Sample' : 'Hidden'}
                      </h5>
                      <section>
                        <h4>Input</h4>
                        <pre className="output-block">{tc.input || <em>(empty)</em>}</pre>
                      </section>
                      <section>
                        <h4>Expected output</h4>
                        <pre className="output-block">{tc.expectedOutput || <em>(empty)</em>}</pre>
                      </section>
                    </div>
                  ))
                ) : (
                  <p className="muted">No sample test cases.</p>
                )}
              </div>
            )}
          </div>
        </div>

        {/* RIGHT PANE — editor on top, console on bottom */}
        <div className="lc-pane lc-pane-right">
          <div className="lc-editor-section">
            <div className="lc-editor-toolbar">
              <select
                value={language}
                onChange={handleLanguageChange}
                className="lc-lang-select"
              >
                <option value="PYTHON">Python</option>
                <option value="JAVA">Java</option>
                <option value="JAVASCRIPT">JavaScript</option>
              </select>
            </div>
            <div className="lc-editor-host">
              <Editor
                height="100%"
                theme="vs-dark"
                language={LANG[language]}
                value={code}
                onChange={(v) => setCode(v ?? '')}
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  scrollBeyondLastLine: false,
                  automaticLayout: true
                }}
              />
            </div>
          </div>

          <div className="lc-console-section">
            <div className="lc-tabs">
              <button
                className={`lc-tab ${bottomTab === 'testcases' ? 'is-active' : ''}`}
                onClick={() => setBottomTab('testcases')}
              >
                <FlaskConical size={14} /> Testcase
              </button>
              <button
                className={`lc-tab ${bottomTab === 'result' ? 'is-active' : ''}`}
                onClick={() => setBottomTab('result')}
              >
                <Terminal size={14} /> Result
                {result && (
                  <span className={`lc-tab-dot ${result.status === 'ACCEPTED' ? 'is-pass' : 'is-fail'}`} />
                )}
              </button>
            </div>

            <div className="lc-console-body">
              {bottomTab === 'testcases' && (
                <div className="lc-test-preview">
                  {question.sampleTestCases?.length ? (
                    question.sampleTestCases.map((tc, i) => (
                      <div className="lc-test-card" key={tc.id}>
                        <div className="lc-test-card-title">Case {i + 1}</div>
                        <div className="lc-kv">
                          <label>Input</label>
                          <pre className="output-block">{tc.input || <em>(empty)</em>}</pre>
                        </div>
                        <div className="lc-kv">
                          <label>Expected</label>
                          <pre className="output-block">{tc.expectedOutput || <em>(empty)</em>}</pre>
                        </div>
                      </div>
                    ))
                  ) : (
                    <p className="muted">No sample test cases.</p>
                  )}
                </div>
              )}

              {bottomTab === 'result' && (
                <div className="lc-result">
                  {submitting && (
                    <p className="muted">Running against all sample + hidden test cases…</p>
                  )}
                  {!submitting && !result && (
                    <p className="muted">Submit your solution to see results here.</p>
                  )}
                  {result && (
                    <>
                      <div className="lc-result-header">
                        <span className={`lc-verdict ${result.status === 'ACCEPTED' ? 'is-pass' : 'is-fail'}`}>
                          {result.status === 'ACCEPTED'
                            ? <CheckCircle2 size={18} />
                            : <XCircle size={18} />}
                          {result.status}
                        </span>
                        <span className="badge badge-neutral">
                          {result.passedCount} / {result.totalCount} passed
                        </span>
                        <span className="badge badge-neutral">
                          Hidden: {result.hiddenPassedCount} / {result.hiddenTotalCount}
                        </span>
                        <span className="badge badge-neutral">{result.totalExecutionTimeMs} ms</span>
                      </div>

                      {result.sampleResults?.length ? (
                        <div className="lc-test-preview">
                          {result.sampleResults.map((r) => (
                            <div className="lc-test-card" key={r.testCaseId}>
                              <div className="lc-test-card-title">
                                <span className="badge badge-neutral">#{r.orderIndex}</span>
                                <span className={`badge ${r.passed ? 'badge-success' : 'badge-error'}`}>
                                  {r.passed ? 'PASS' : 'FAIL'}
                                </span>
                                <span className="muted">{r.executionTimeMs} ms · exit {r.exitCode}</span>
                              </div>
                              <div className="lc-kv">
                                <label>Input</label>
                                <pre className="output-block">{r.input || <em>(empty)</em>}</pre>
                              </div>
                              <div className="lc-kv">
                                <label>Expected</label>
                                <pre className="output-block">{r.expectedOutput || <em>(empty)</em>}</pre>
                              </div>
                              <div className="lc-kv">
                                <label>Actual stdout</label>
                                <pre className="output-block">{r.actualOutput || <em>(empty)</em>}</pre>
                              </div>
                              {r.stderr && (
                                <div className="lc-kv">
                                  <label>stderr</label>
                                  <pre className="output-block stderr">{r.stderr}</pre>
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      ) : null}
                    </>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
