import React from 'react';
import { Threat, ThreatStatus } from '../../types/threat';
import { SeverityBadge } from '../common/SeverityBadge';
import { StatusBadge } from '../common/StatusBadge';
import { formatTimestamp, getThreatTypeLabel } from '../../utils/formatters';
import { useNavigate } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';

interface ThreatTableProps {
  threats: Threat[];
  onStatusChange?: (threat: Threat, newStatus: ThreatStatus) => void;
}

export const ThreatTable: React.FC<ThreatTableProps> = ({ threats }) => {
  const navigate = useNavigate();

  return (
    <div className="panel overflow-hidden">
      <div className="overflow-x-auto">
        <table className="soc-table">
          <thead>
            <tr>
              <th>Severity</th>
              <th>Threat Type</th>
              <th>Application</th>
              <th>Endpoint</th>
              <th>Risk / Conf</th>
              <th>Status</th>
              <th>Detected At</th>
              <th className="text-right">Action</th>
            </tr>
          </thead>
          <tbody>
            {threats.map((threat) => (
              <tr
                key={threat.id}
                onClick={() => navigate(`/threats/${threat.id}`)}
                className="cursor-pointer"
              >
                <td><SeverityBadge severity={threat.severity} /></td>
                <td className="font-semibold text-slate-900">{getThreatTypeLabel(threat.threatType)}</td>
                <td className="font-mono font-semibold text-blue-700">{threat.applicationId}</td>
                <td>
                  <span className="code-badge">{threat.httpMethod} {threat.endpoint}</span>
                </td>
                <td className="font-mono">
                  <div className="flex flex-col gap-0.5">
                    <span className="text-slate-800 font-semibold">{threat.riskScore.toFixed(2)}</span>
                    <span className="text-xs text-slate-400">{(threat.confidence * 100).toFixed(0)}% conf</span>
                  </div>
                </td>
                <td><StatusBadge status={threat.status} /></td>
                <td className="font-mono text-slate-500 whitespace-nowrap">{formatTimestamp(threat.detectedAt)}</td>
                <td className="text-right">
                  <button
                    onClick={(e) => { e.stopPropagation(); navigate(`/threats/${threat.id}`); }}
                    className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-800 text-xs font-semibold"
                  >
                    Investigate <ArrowRight size={11} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
