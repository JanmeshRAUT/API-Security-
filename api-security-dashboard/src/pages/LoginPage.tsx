import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, Lock, Mail, ArrowRight, ShieldAlert, Eye, EyeOff, Radio } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const LoginPage: React.FC = () => {
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!email || !password) { setError('Email and password are required.'); return; }
    if (isRegister && password !== confirmPassword) { setError('Passwords do not match.'); return; }
    setLoading(true);
    try {
      if (isRegister) await register(email, password);
      else await login(email, password);
      navigate('/');
    } catch {
      setError('Authentication failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-white">

      {/* ── LEFT: Dark info panel ── */}
      <div
        className="hidden lg:flex lg:w-[40%] flex-col bg-slate-950 text-white relative overflow-hidden"
        style={{
          backgroundImage: `
            linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px)
          `,
          backgroundSize: '24px 24px',
        }}
      >
        {/* Brand */}
        <div className="relative z-10 flex items-center gap-2.5 px-8 pt-8">
          <div className="flex items-center justify-center w-7 h-7 rounded bg-blue-600">
            <ShieldCheck size={15} className="text-white" />
          </div>
          <div>
            <p className="text-xs font-semibold text-white tracking-widest uppercase font-mono">API Thread SOC</p>
            <p className="text-2xs text-slate-500 font-mono tracking-widest uppercase">Security Operations</p>
          </div>
        </div>

        {/* Main copy */}
        <div className="relative z-10 flex-1 flex flex-col justify-center px-8 py-10">
          <div className="flex items-center gap-2 mb-6">
            <span className="live-dot" />
            <span className="text-xs font-mono text-emerald-400 uppercase tracking-widest">SOC Pipeline Online</span>
          </div>

          <h1 className="text-2xl font-bold text-white leading-snug mb-2 font-display">
            API Threat Intelligence<br />
            & Thread Monitoring
          </h1>
          <p className="text-sm text-slate-400 leading-relaxed mb-8">
            Monitor, detect and respond to API threats across Java, Node.js and Python microservices.
          </p>

          {/* Metrics — 2×2 compact grid */}
          <div className="grid grid-cols-2 gap-2.5 mb-8">
            {[
              { label: 'Endpoints Monitored', val: '2,814' },
              { label: 'Threats Blocked', val: '147K+' },
              { label: 'Mean Detect Time', val: '< 800ms' },
              { label: 'Uptime SLA', val: '99.97%' },
            ].map((m) => (
              <div key={m.label} className="border border-slate-800 rounded bg-slate-900/60 px-3 py-2.5">
                <div className="text-sm font-semibold text-white font-mono">{m.val}</div>
                <div className="text-2xs text-slate-500 mt-0.5 uppercase tracking-wide">{m.label}</div>
              </div>
            ))}
          </div>

          {/* Stack */}
          <div className="flex flex-wrap gap-1.5">
            {['Java Spring Boot', 'Node.js Express', 'Python FastAPI'].map((s) => (
              <span key={s} className="code-badge text-slate-400 border-slate-700 bg-slate-800/80">{s}</span>
            ))}
          </div>
        </div>

        {/* Footer */}
        <div className="relative z-10 px-8 py-4 border-t border-slate-800 flex items-center justify-between">
          <span className="text-2xs text-slate-600 font-mono">v2.4.1 Production</span>
          <span className="text-2xs text-slate-600 font-mono">AES-256 · TLS 1.3</span>
        </div>
      </div>

      {/* ── RIGHT: Auth form ── */}
      <div className="flex-1 flex flex-col">

        {/* Top strip: mobile logo + tab switcher */}
        <div className="flex items-center justify-between px-6 py-3 border-b border-slate-100">
          <div className="flex items-center gap-2 lg:hidden">
            <div className="flex items-center justify-center w-6 h-6 rounded bg-blue-600">
              <ShieldCheck size={13} className="text-white" />
            </div>
            <span className="text-xs font-semibold text-slate-900 uppercase tracking-widest font-mono">API Thread SOC</span>
          </div>
          <div className="lg:ml-auto flex items-center gap-0.5 bg-slate-100 rounded p-0.5 border border-slate-200">
            {['Sign In', 'Register'].map((tab, i) => (
              <button
                key={tab}
                type="button"
                onClick={() => { setIsRegister(i === 1); setError(''); }}
                className={`px-3.5 py-1 rounded text-xs font-medium transition-all duration-100 ${
                  isRegister === (i === 1)
                    ? 'bg-white text-slate-900 shadow-sm border border-slate-200'
                    : 'text-slate-500 hover:text-slate-700'
                }`}
              >
                {tab}
              </button>
            ))}
          </div>
        </div>

        {/* Form — centered */}
        <div className="flex-1 flex items-center justify-center px-8 py-8">
          <div className="w-full max-w-xs">

            <div className="mb-6">
              <h2 className="text-xl font-bold text-slate-900 font-display">
                {isRegister ? 'Create Operator Account' : 'Sign in to Console'}
              </h2>
              <p className="text-sm text-slate-500 mt-1">
                {isRegister
                  ? 'Register a new operator account to access SOC monitoring.'
                  : 'Enter your credentials to access the security console.'}
              </p>
            </div>

            {error && (
              <div className="mb-4 flex items-start gap-2 p-2.5 bg-red-50 border border-red-200 rounded">
                <ShieldAlert size={13} className="text-red-600 mt-0.5 shrink-0" />
                <p className="text-xs text-red-700">{error}</p>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-3.5">
              {/* Email */}
              <div>
                <label className="soc-label">Email</label>
                <div className="relative">
                  <Mail size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    id="login-email"
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="analyst@secops.corp"
                    className="soc-input pl-8"
                    autoComplete="email"
                  />
                </div>
              </div>

              {/* Password */}
              <div>
                <label className="soc-label">Password</label>
                <div className="relative">
                  <Lock size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    id="login-password"
                    type={showPass ? 'text' : 'password'}
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••••••"
                    className="soc-input pl-8 pr-8"
                    autoComplete={isRegister ? 'new-password' : 'current-password'}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPass(!showPass)}
                    className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  >
                    {showPass ? <EyeOff size={13} /> : <Eye size={13} />}
                  </button>
                </div>
              </div>

              {/* Confirm Password */}
              {isRegister && (
                <div>
                  <label className="soc-label">Confirm Password</label>
                  <div className="relative">
                    <Lock size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
                    <input
                      id="login-confirm-password"
                      type="password"
                      required
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Re-enter password"
                      className="soc-input pl-8"
                      autoComplete="new-password"
                    />
                  </div>
                </div>
              )}

              <button
                id="login-submit"
                type="submit"
                disabled={loading}
                className="w-full mt-1 flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white text-xs font-semibold rounded transition-colors"
              >
                {loading ? (
                  <span className="flex items-center gap-1.5">
                    <svg className="animate-spin w-3.5 h-3.5" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                    </svg>
                    Authenticating...
                  </span>
                ) : (
                  <>
                    <span>{isRegister ? 'Create Account' : 'Sign In'}</span>
                    <ArrowRight size={13} />
                  </>
                )}
              </button>
            </form>

            <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
              <div className="flex items-center gap-1.5">
                <Radio size={9} className="text-emerald-500 animate-pulse" />
                <span className="text-2xs text-slate-400">SOC Engine Active</span>
              </div>
              <span className="text-2xs font-mono text-slate-400">SHA-256 auth</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
