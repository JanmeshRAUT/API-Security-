import React, { useState } from 'react';
import { X, Plus, Key } from 'lucide-react';
import { ApplicationApi } from '../../api/applications';
import { ApplicationRegistrationResponse } from '../../types/application';

interface RegisterAppModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (registeredApp: ApplicationRegistrationResponse) => void;
}

export const RegisterAppModal: React.FC<RegisterAppModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [name, setName] = useState('');
  const [applicationId, setApplicationId] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !applicationId) {
      setError('Name and Application ID are required.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const res = await ApplicationApi.register({ name, applicationId, description });
      onSuccess(res);
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to register application');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs">
      <div className="panel max-w-md w-full p-6 shadow-2xl border border-slate-200">
        <div className="flex items-center justify-between pb-4 border-b border-slate-200 shrink-0">
          <div className="flex items-center gap-2 text-slate-800">
            <div className="p-2 bg-blue-50 text-blue-600 rounded-md">
              <Plus size={18} />
            </div>
            <h3 className="text-base font-bold">Register New Application</h3>
          </div>
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-slate-800 hover:bg-slate-100 rounded-md transition-colors">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-5 space-y-4 text-sm">
          {error && (
            <div className="p-3 bg-rose-50 text-rose-700 text-xs rounded border border-rose-200 font-mono">
              {error}
            </div>
          )}

          <div>
            <label className="soc-label">Application Name *</label>
            <input
              type="text"
              required
              placeholder="e.g. Order Processing Service"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="soc-input w-full"
            />
          </div>

          <div>
            <label className="soc-label">Application ID *</label>
            <input
              type="text"
              required
              placeholder="e.g. order-service"
              value={applicationId}
              onChange={(e) => setApplicationId(e.target.value.toLowerCase().replace(/\s+/g, '-'))}
              className="soc-input w-full font-mono"
            />
            <p className="text-[11px] text-slate-500 mt-1">Unique slug used by telemetry starter / middleware</p>
          </div>

          <div>
            <label className="soc-label">Description</label>
            <textarea
              placeholder="Application overview..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              className="soc-input w-full resize-none"
            />
          </div>

          <div className="pt-4 flex justify-end gap-3 border-t border-slate-100">
            <button type="button" onClick={onClose} className="btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="btn-primary">
              <Key size={14} />
              <span>{loading ? 'Registering...' : 'Register & Generate Key'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
