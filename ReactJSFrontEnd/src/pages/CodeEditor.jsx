import { useState } from 'react';
import Editor from '@monaco-editor/react';
import { Play, Save } from 'lucide-react';
import { executionApi } from '../api/executionApi';
import { snippetApi } from '../api/snippetApi';
import { extractErrorMessage } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage';
import OutputPanel from '../components/OutputPanel';

const LANGUAGES = [
  { value: 'PYTHON',     label: 'Python',     monaco: 'python',     sample: 'print("Hello, CodeRank!")\n' },
  { value: 'JAVA',       label: 'Java',       monaco: 'java',       sample: 'public class Main {\n  public static void main(String[] args) {\n    System.out.println("Hello, CodeRank!");\n  }\n}\n' },
  { value: 'JAVASCRIPT', label: 'JavaScript', monaco: 'javascript', sample: 'console.log("Hello, CodeRank!");\n' }
];

export default function CodeEditor() {
  const [language, setLanguage] = useState('PYTHON');
  const langDef = LANGUAGES.find((l) => l.value === language);
  const [code, setCode] = useState(langDef.sample);
  const [stdin, setStdin] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [running, setRunning] = useState(false);
  const [savedSnippet, setSavedSnippet] = useState(null);
  const [saving, setSaving] = useState(false);

  const handleLanguageChange = (e) => {
    const next = e.target.value;
    setLanguage(next);
    const def = LANGUAGES.find((l) => l.value === next);
    if (def) setCode(def.sample);
  };

  const handleRun = async () => {
    setError(''); setResult(null); setRunning(true);
    try {
      const data = await executionApi.execute({ language, code, stdin });
      setResult(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setRunning(false);
    }
  };

  const handleSave = async () => {
    setSaving(true); setError(''); setSavedSnippet(null);
    try {
      const title = prompt('Snippet title?', `Snippet ${new Date().toLocaleString()}`);
      if (!title) { setSaving(false); return; }
      const data = await snippetApi.create({ title, language, code });
      setSavedSnippet(data);
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
          <h1>Code Editor</h1>
          <p className="page-subtitle">Run code in a sandboxed Docker container.</p>
        </div>
      </div>

      <ErrorMessage message={error} onDismiss={() => setError('')} />

      <div className="editor-layout">
        <div className="card editor-card">
          <div className="editor-toolbar">
            <label htmlFor="lang" className="muted">Language</label>
            <select id="lang" value={language} onChange={handleLanguageChange} style={{ width: 'auto' }}>
              {LANGUAGES.map((l) => <option key={l.value} value={l.value}>{l.label}</option>)}
            </select>
            <div style={{ flex: 1 }} />
            <button className="btn btn-ghost" onClick={handleSave} disabled={saving || !code.trim()}>
              <Save size={16} /> {saving ? 'Saving…' : 'Save as Snippet'}
            </button>
            <button className="btn btn-primary" onClick={handleRun} disabled={running || !code.trim()}>
              <Play size={16} /> {running ? 'Running…' : 'Run'}
            </button>
          </div>
          <div className="editor-host">
            <Editor
              height="100%"
              theme="vs-dark"
              language={langDef.monaco}
              value={code}
              onChange={(v) => setCode(v ?? '')}
              options={{ minimap: { enabled: false }, fontSize: 14 }}
            />
          </div>
        </div>

        <div className="col">
          <div className="card">
            <h3 style={{ margin: '0 0 8px' }}>Standard input (stdin)</h3>
            <textarea
              rows={6}
              placeholder="Lines piped to the program's stdin (optional)"
              value={stdin}
              onChange={(e) => setStdin(e.target.value)}
            />
          </div>

          {savedSnippet && (
            <div className="card">
              <h3 style={{ margin: '0 0 8px' }}>Saved snippet</h3>
              <p className="mono">#{savedSnippet.id} — {savedSnippet.title}</p>
            </div>
          )}

          <div className="card">
            <h3 style={{ margin: '0 0 8px' }}>Output</h3>
            {!result && !running && <p className="muted">Run the code to see output here.</p>}
            {running && <p className="muted">Executing in sandbox…</p>}
            <OutputPanel result={result} />
          </div>
        </div>
      </div>
    </div>
  );
}
