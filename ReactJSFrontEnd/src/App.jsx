import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import CodeEditor from './pages/CodeEditor';
import Snippets from './pages/Snippets';
import SnippetDetail from './pages/SnippetDetail';
import Submissions from './pages/Submissions';
import SubmissionDetail from './pages/SubmissionDetail';
import Questions from './pages/Questions';
import QuestionDetail from './pages/QuestionDetail';
import Solutions from './pages/Solutions';
import AdminQuestionForm from './pages/AdminQuestionForm';
import Health from './pages/Health';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="app-shell">
          <Navbar />
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route path="/" element={
              <ProtectedRoute><Dashboard /></ProtectedRoute>
            } />
            <Route path="/editor" element={
              <ProtectedRoute><CodeEditor /></ProtectedRoute>
            } />
            <Route path="/snippets" element={
              <ProtectedRoute><Snippets /></ProtectedRoute>
            } />
            <Route path="/snippets/:id" element={
              <ProtectedRoute><SnippetDetail /></ProtectedRoute>
            } />
            <Route path="/submissions" element={
              <ProtectedRoute><Submissions /></ProtectedRoute>
            } />
            <Route path="/submissions/:id" element={
              <ProtectedRoute><SubmissionDetail /></ProtectedRoute>
            } />
            <Route path="/questions" element={
              <ProtectedRoute><Questions /></ProtectedRoute>
            } />
            <Route path="/questions/:id" element={
              <ProtectedRoute><QuestionDetail /></ProtectedRoute>
            } />
            <Route path="/solutions" element={
              <ProtectedRoute><Solutions /></ProtectedRoute>
            } />
            <Route path="/admin/questions/new" element={
              <ProtectedRoute adminOnly><AdminQuestionForm /></ProtectedRoute>
            } />
            <Route path="/health" element={
              <ProtectedRoute><Health /></ProtectedRoute>
            } />

            <Route path="*" element={<NotFound />} />
          </Routes>
        </div>
      </AuthProvider>
    </BrowserRouter>
  );
}
