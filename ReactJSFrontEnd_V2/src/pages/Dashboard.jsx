import { Link } from 'react-router-dom';
import { Code, Activity, BookOpen, Trophy, Shield } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const TILES = [
  { to: '/editor',      title: 'Code Editor',  desc: 'Run Python, Java, or JavaScript in a sandbox.', icon: Code },
  { to: '/questions',   title: 'Questions',    desc: 'Browse questions and submit solutions.', icon: BookOpen },
  { to: '/solutions',   title: 'Submissions',  desc: 'Your grading attempts and verdicts.', icon: Trophy },
  { to: '/health',      title: 'API Status',   desc: 'Check that the backend is reachable.', icon: Activity }
];

export default function Dashboard() {
  const { user, isAdmin } = useAuth();
  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Hello, {user?.username || 'coder'} 👋</h1>
          <p className="page-subtitle">Welcome to CodeRank.</p>
        </div>
      </div>
      <div className="tile-grid">
        {TILES.map(({ to, title, desc, icon: Icon }) => (
          <Link to={to} key={to} className="tile">
            <div className="card">
              <div className="row spread">
                <div>
                  <div className="tile-title">{title}</div>
                  <div className="tile-desc">{desc}</div>
                </div>
                <Icon size={28} color="var(--primary)" />
              </div>
            </div>
          </Link>
        ))}
        {isAdmin && (
          <Link to="/admin/questions/new" className="tile">
            <div className="card">
              <div className="row spread">
                <div>
                  <div className="tile-title">Author Question</div>
                  <div className="tile-desc">Admin-only: create or edit a question with test cases.</div>
                </div>
                <Shield size={28} color="var(--primary)" />
              </div>
            </div>
          </Link>
        )}
      </div>
    </div>
  );
}
