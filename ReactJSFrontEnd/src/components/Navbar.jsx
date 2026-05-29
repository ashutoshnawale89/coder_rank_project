import { NavLink, useNavigate } from 'react-router-dom';
import { Code2, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <NavLink to="/" className="brand">
          <Code2 size={20} /> <span>CodeRank</span>
        </NavLink>

        <div className="nav-links">
          {isAuthenticated && (
            <>
              <NavLink to="/" end>Dashboard</NavLink>
              <NavLink to="/editor">Editor</NavLink>
              <NavLink to="/snippets">Snippets</NavLink>
              <NavLink to="/submissions">Submissions</NavLink>
              <NavLink to="/questions">Questions</NavLink>
              <NavLink to="/solutions">Solutions</NavLink>
              <NavLink to="/health">Health</NavLink>
              {isAdmin && <NavLink to="/admin/questions/new">New Question</NavLink>}
            </>
          )}
        </div>

        <div className="nav-user">
          {isAuthenticated ? (
            <>
              <span className="user-chip">
                {user?.username}
                {isAdmin && <span className="role-badge">ADMIN</span>}
              </span>
              <button className="btn btn-ghost" onClick={handleLogout} title="Logout">
                <LogOut size={16} /> Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="btn btn-ghost">Login</NavLink>
              <NavLink to="/register" className="btn btn-primary">Register</NavLink>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
