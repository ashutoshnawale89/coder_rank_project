import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api/authApi';

const TOKEN_KEY = 'cr_token';
const USER_KEY = 'cr_user';

const AuthContext = createContext(null);

function decodeRoleFromToken(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload?.role || 'USER';
  } catch {
    return 'USER';
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  }, [token]);

  useEffect(() => {
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
    else localStorage.removeItem(USER_KEY);
  }, [user]);

  const applyAuthResult = useCallback((data) => {
    if (!data?.token) throw new Error('No token returned from server');
    setToken(data.token);
    setUser({
      userId: data.userId,
      username: data.username,
      role: decodeRoleFromToken(data.token),
      expiresInMs: data.expiresInMs
    });
  }, []);

  const login = useCallback(async (creds) => applyAuthResult(await authApi.login(creds)), [applyAuthResult]);
  const register = useCallback(async (form) => applyAuthResult(await authApi.register(form)), [applyAuthResult]);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(() => ({
    token,
    user,
    isAuthenticated: !!token,
    isAdmin: user?.role === 'ADMIN',
    login,
    register,
    logout
  }), [token, user, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
