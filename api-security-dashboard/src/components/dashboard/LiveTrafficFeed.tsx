import React from 'react';
import { useSecurityContext } from '../../context/SecurityContext';
import { formatTimestamp } from '../../utils/formatters';
import { Activity } from 'lucide-react';

const METHOD_COLORS: Record<string, string> = {
  GET:    'bg-sky-50 text-sky-700 border-sky-200',
  POST:   'bg-violet-50 text-violet-700 border-violet-200',
  PUT:    'bg-amber-50 text-amber-700 border-amber-200',
  PATCH:  'bg-orange-50 text-orange-700 border-orange-200',
  DELETE: 'bg-red-50 text-red-700 border-red-200',
};

const statusColor = (code: number) => {
  if (code >= 200 && code < 300) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (code >= 400 && code < 500) return 'bg-amber-50 text-amber-700 border-amber-200';
  if (code >= 500)               return 'bg-red-50 text-red-700 border-red-200';
  return 'bg-slate-50 text-slate-600 border-slate-200';
};

export const LiveTrafficFeed: React.FC = () => {
  const { liveEvents } = useSecurityContext();

  return (
    <div className="panel flex flex-col h-full overflow-hidden">
      <div className="panel-header shrink-0">
        <div className="flex items-center gap-1.5">
          <Activity size={12} className="text-blue-500 animate-pulse" />
          <span className="panel-title">API Traffic Stream</span>
        </div>
        <span className="status-pill status-neutral">{liveEvents.length} events</span>
      </div>

      <div className="flex-1 overflow-auto">
        {liveEvents.length === 0 ? (
          <div className="empty-state">
            <Activity size={22} className="text-slate-300" />
            <p>Listening for API events...</p>
            <span>/topic/security/events</span>
          </div>
        ) : (
          <table className="soc-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>App</th>
                <th>Method</th>
                <th>Endpoint</th>
                <th>Status</th>
                <th>ms</th>
                <th>Auth</th>
              </tr>
            </thead>
            <tbody>
              {liveEvents.map((evt, idx) => (
                <tr key={`${evt.id}-${idx}`}>
                  <td className="font-mono text-xs text-slate-400 whitespace-nowrap">
                    {formatTimestamp(evt.timestamp)}
                  </td>
                  <td className="font-mono font-semibold text-slate-800">{evt.applicationId}</td>
                  <td>
                    <span className={`status-pill border ${METHOD_COLORS[evt.httpMethod] ?? 'bg-slate-50 text-slate-600 border-slate-200'}`}>
                      {evt.httpMethod}
                    </span>
                  </td>
                  <td className="font-mono text-slate-600 truncate max-w-[110px]" title={evt.endpoint}>
                    {evt.endpoint}
                  </td>
                  <td>
                    <span className={`status-pill border ${statusColor(evt.statusCode)}`}>
                      {evt.statusCode}
                    </span>
                  </td>
                  <td className="font-mono text-slate-600">{evt.responseTimeMs}</td>
                  <td className="font-mono">
                    {evt.authenticated
                      ? <span className="text-emerald-600 font-semibold">✓</span>
                      : <span className="text-slate-300">—</span>
                    }
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};
