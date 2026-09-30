import React, { useState } from 'react';
import { X, Key, Copy, Check, ShieldAlert } from 'lucide-react';
import { ApplicationRegistrationResponse } from '../../types/application';

interface AppApiKeyModalProps {
  appData: ApplicationRegistrationResponse | null;
  onClose: () => void;
}

export const AppApiKeyModal: React.FC<AppApiKeyModalProps> = ({ appData, onClose }) => {
  const [copied, setCopied] = useState(false);

  if (!appData) return null;

  const copyToClipboard = () => {
    navigator.clipboard.writeText(appData.apiKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs">
      <div className="panel max-w-lg w-full p-6 shadow-2xl border border-slate-200">
        <div className="flex items-center justify-between pb-4 border-b border-slate-200 shrink-0">
          <div className="flex items-center gap-2 text-emerald-700">
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-md">
              <Key size={18} />
            </div>
            <h3 className="text-base font-bold">API Key Generated Successfully</h3>
          </div>
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-slate-800 hover:bg-slate-100 rounded-md transition-colors">
            <X size={18} />
          </button>
        </div>

        <div className="mt-5 space-y-5 text-sm">
          <div className="p-3 bg-amber-50 border border-amber-200 text-amber-900 rounded-lg text-xs flex items-start gap-2">
            <ShieldAlert size={16} className="text-amber-600 shrink-0 mt-0.5" />
            <div>
              <strong className="block mb-0.5">IMPORTANT: Save your API key now.</strong>
              <p>This API Key will never be displayed again. If lost, you will need to regenerate a new key.</p>
            </div>
          </div>

          <div>
            <label className="soc-label">Application ID</label>
            <div className="font-mono text-sm font-bold text-slate-800 p-2.5 bg-slate-50 rounded-lg border border-slate-200">
              {appData.applicationId}
            </div>
          </div>

          <div>
            <label className="soc-label">Generated API Key (Secret)</label>
            <div className="flex items-center gap-2">
              <input
                type="text"
                readOnly
                value={appData.apiKey}
                className="soc-input w-full font-mono text-xs font-bold text-emerald-600 bg-slate-50"
              />
              <button
                onClick={copyToClipboard}
                className="btn-primary py-2 px-3 shrink-0"
              >
                {copied ? <Check size={14} /> : <Copy size={14} />}
                <span>{copied ? 'Copied!' : 'Copy'}</span>
              </button>
            </div>
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <button
              onClick={onClose}
              className="btn-primary"
            >
              Done & Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
