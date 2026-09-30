import React from 'react';
import { AlertTriangle, X } from 'lucide-react';
import { useSecurityContext } from '../../context/SecurityContext';
import { getThreatTypeLabel } from '../../utils/formatters';
import { useNavigate } from 'react-router-dom';

export const ToastNotification: React.FC = () => {
  const { toasts, dismissToast } = useSecurityContext();
  const navigate = useNavigate();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-5 right-5 z-50 flex flex-col space-y-3 max-w-sm w-full">
      {toasts.map(({ id, threat }) => (
        <div
          key={id}
          className={`p-4 rounded-lg shadow-xl border border-l-4 bg-white transition-all transform translate-y-0 ${
            threat.severity === 'CRITICAL'
              ? 'border-rose-600 border-l-rose-600 bg-rose-50/20'
              : 'border-amber-500 border-l-amber-500 bg-amber-50/20'
          }`}
        >
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-2">
              <AlertTriangle className={threat.severity === 'CRITICAL' ? 'text-rose-600' : 'text-amber-600'} size={20} />
              <span className={`text-xs font-bold uppercase tracking-wider ${
                threat.severity === 'CRITICAL' ? 'text-rose-700' : 'text-amber-700'
              }`}>
                ⚠ {threat.severity} THREAT DETECTED
              </span>
            </div>
            <button
              onClick={() => dismissToast(id)}
              className="text-slate-400 hover:text-slate-600 transition-colors"
            >
              <X size={16} />
            </button>
          </div>

          <div className="mt-2 text-sm font-semibold text-slate-900">
            {getThreatTypeLabel(threat.threatType)}
          </div>

          <div className="mt-1 text-xs text-slate-600 font-mono">
            App: <span className="font-semibold text-slate-800">{threat.applicationId}</span>
          </div>

          <div className="mt-2 flex items-center justify-between text-xs pt-2 border-t border-slate-100">
            <span className="font-mono text-slate-500">Risk: <strong className="text-slate-800">{threat.riskScore.toFixed(2)}</strong></span>
            <button
              onClick={() => {
                dismissToast(id);
                navigate(`/threats/${threat.id}`);
              }}
              className="text-brand-600 hover:text-brand-700 font-semibold hover:underline"
            >
              Investigate →
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};
