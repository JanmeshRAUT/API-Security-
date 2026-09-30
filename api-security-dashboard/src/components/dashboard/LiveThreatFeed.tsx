import React from 'react';
import { useSecurityContext } from '../../context/SecurityContext';
import { SeverityBadge } from '../common/SeverityBadge';
import { formatRelativeTime, getThreatTypeLabel } from '../../utils/formatters';
import { Radio, ArrowRight, ShieldAlert } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const LiveThreatFeed: React.FC = () => {
  const { liveThreats } = useSecurityContext();
  const navigate = useNavigate();

  return (
    <div className="panel flex flex-col h-full overflow-hidden">
      <div className="panel-header shrink-0">
        <div className="flex items-center gap-1.5">
          <Radio size={12} className="text-red-500 animate-pulse" />
          <span className="panel-title">Live Threat Feed</span>
        </div>
        <span className={`status-pill ${liveThreats.length > 0 ? 'status-critical' : 'status-safe'}`}>
          {liveThreats.length} detected
        </span>
      </div>

      <div className="flex-1 overflow-y-auto divide-y divide-slate-50">
        {liveThreats.length === 0 ? (
          <div className="empty-state">
            <ShieldAlert size={22} className="text-slate-300" />
            <p>Listening for threat events...</p>
            <span>/topic/security/threats</span>
          </div>
        ) : (
          liveThreats.map((threat, index) => (
            <div
              key={`${threat.id}-${index}`}
              onClick={() => navigate(`/threats/${threat.id}`)}
              className="px-4 py-2.5 hover:bg-slate-50 cursor-pointer transition-colors group"
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-1.5 flex-1 min-w-0">
                  <SeverityBadge severity={threat.severity} size="sm" />
                  <span className="text-sm font-medium text-slate-800 truncate">
                    {getThreatTypeLabel(threat.threatType)}
                  </span>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  <span className="text-xs text-slate-400 font-mono">
                    {formatRelativeTime(threat.detectedAt)}
                  </span>
                  <ArrowRight size={11} className="text-slate-300 group-hover:text-blue-500 transition-colors" />
                </div>
              </div>
              <div className="mt-1 flex items-center gap-2 text-xs text-slate-500 font-mono">
                <span>{threat.applicationId}</span>
                <span className="code-badge">{threat.httpMethod}</span>
                <span className="truncate">{threat.endpoint}</span>
                <span className="ml-auto text-red-600 font-semibold">{threat.riskScore.toFixed(2)}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
