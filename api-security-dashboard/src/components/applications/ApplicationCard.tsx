import React from 'react';
import { Application } from '../../types/application';
import { StatusBadge } from '../common/StatusBadge';
import { formatRelativeTime } from '../../utils/formatters';
import { AppWindow, ArrowRight, BookOpen } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface ApplicationCardProps {
  app: Application;
  onOpenGuide?: (app: Application) => void;
}

export const ApplicationCard: React.FC<ApplicationCardProps> = ({ app, onOpenGuide }) => {
  const navigate = useNavigate();

  return (
    <div className="panel hover:border-slate-300 hover:shadow-sm transition-all flex flex-col">
      <div className="p-4 flex-1">
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-50 rounded-md">
              <AppWindow size={18} className="text-blue-600" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-slate-900 leading-tight">{app.name}</h3>
              <span className="text-xs font-mono text-slate-400">{app.applicationId}</span>
            </div>
          </div>
          <StatusBadge status={app.status as any} />
        </div>

        {app.description && (
          <p className="text-sm text-slate-500 line-clamp-2 mt-2">{app.description}</p>
        )}

        <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between">
          <div className="text-xs text-slate-500 font-mono">
            Last seen: <span className="text-slate-700 font-medium">{formatRelativeTime(app.lastSeenAt)}</span>
          </div>
        </div>
      </div>

      <div className="px-4 py-3 border-t border-slate-100 bg-slate-50/50 flex items-center gap-3">
        {onOpenGuide && (
          <button
            onClick={() => onOpenGuide(app)}
            className="btn-secondary py-1.5 text-xs flex-1 justify-center"
          >
            <BookOpen size={12} />
            Setup Guide
          </button>
        )}
        <button
          onClick={() => navigate(`/applications/${app.id}`)}
          className="btn-primary py-1.5 text-xs flex-1 justify-center"
        >
          Manage
          <ArrowRight size={12} />
        </button>
      </div>
    </div>
  );
};
