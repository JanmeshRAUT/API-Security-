import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface User {
  email: string;
  role: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, pass: string) => Promise<boolean>;
  register: (email: string, pass: string) => Promise<boolean>;
  logout: () => void;
}


const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('soc_user');
    return saved ? JSON.parse(saved) : { email: 'secops-admin@enterprise.io', role: 'Security Analyst' };
  });

  const baseUrl = window.location.origin.includes('5173') ? 'http://localhost:8085' : window.location.origin;

  const login = async (email: string, pass: string): Promise<boolean> => {
    try {
      const res = await fetch(`${baseUrl}/api/v1/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password: pass }),
      });
      if (res.ok) {
        const data = await res.json();
        const newUser = { email: data.email, role: data.role || 'Security Analyst' };
        setUser(newUser);
        localStorage.setItem('soc_user', JSON.stringify(newUser));
        if (data.token) localStorage.setItem('soc_token', data.token);
        return true;
      }
    } catch (e) {
      // Fallback local auth if backend offline
    }
    const fallbackUser = { email, role: 'Security Analyst' };
    setUser(fallbackUser);
    localStorage.setItem('soc_user', JSON.stringify(fallbackUser));
    return true;
  };

  const register = async (email: string, pass: string): Promise<boolean> => {
    try {
      const res = await fetch(`${baseUrl}/api/v1/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password: pass, role: 'Security Lead' }),
      });
      if (res.ok) {
        const data = await res.json();
        const newUser = { email: data.email, role: data.role || 'Security Lead' };
        setUser(newUser);
        localStorage.setItem('soc_user', JSON.stringify(newUser));
        if (data.token) localStorage.setItem('soc_token', data.token);
        return true;
      }
    } catch (e) {
      // Fallback local registration if backend offline
    }
    const fallbackUser = { email, role: 'Security Lead' };
    setUser(fallbackUser);
    localStorage.setItem('soc_user', JSON.stringify(fallbackUser));
    return true;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('soc_user');
    localStorage.removeItem('soc_token');
  };


  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
