import React from 'react';
import { ThreatFilter, ThreatSeverity, ThreatStatus } from '../../types/threat';
import { Search, Filter, RotateCcw } from 'lucide-react';

interface ThreatFilterBarProps {
  filter: ThreatFilter;
  onChange: (updated: ThreatFilter) => void;
  onReset: () => void;
}

const selectClass = 'soc-input py-1.5 text-sm w-auto';

export const ThreatFilterBar: React.FC<ThreatFilterBarProps> = ({ filter, onChange, onReset }) => {
  return (
    <div className="panel flex flex-wrap items-center gap-3 px-4 py-3 shrink-0">
      <div className="flex items-center gap-2 text-xs font-semibold text-slate-400 uppercase tracking-widest font-mono">
        <Filter size={13} className="text-blue-500" />
        <span>Filters</span>
      </div>

      <select
        value={filter.threatType || ''}
        onChange={(e) => onChange({ ...filter, threatType: e.target.value || undefined, page: 0 })}
        className={selectClass}
      >
        <option value="">All Threat Types</option>
        <option value="CREDENTIAL_STUFFING">Credential Stuffing</option>
        <option value="BOLA">BOLA / Object Access</option>
        <option value="ID_ENUMERATION">ID Enumeration</option>
        <option value="SUSPICIOUS_BEHAVIOR">Suspicious Behavior</option>
      </select>

      <select
        value={filter.severity || ''}
        onChange={(e) => onChange({ ...filter, severity: (e.target.value as ThreatSeverity) || undefined, page: 0 })}
        className={selectClass}
      >
        <option value="">All Severities</option>
        <option value="CRITICAL">CRITICAL</option>
        <option value="HIGH">HIGH</option>
        <option value="MEDIUM">MEDIUM</option>
        <option value="LOW">LOW</option>
      </select>

      <select
        value={filter.status || ''}
        onChange={(e) => onChange({ ...filter, status: (e.target.value as ThreatStatus) || undefined, page: 0 })}
        className={selectClass}
      >
        <option value="">All Statuses</option>
        <option value="OPEN">OPEN</option>
        <option value="ACKNOWLEDGED">ACKNOWLEDGED</option>
        <option value="RESOLVED">RESOLVED</option>
      </select>

      <div className="relative">
        <Search size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Filter by Application ID..."
          value={filter.applicationId || ''}
          onChange={(e) => onChange({ ...filter, applicationId: e.target.value || undefined, page: 0 })}
          className="soc-input pl-8 w-52"
        />
      </div>

      <button
        onClick={onReset}
        className="ml-auto btn-secondary py-1.5"
      >
        <RotateCcw size={12} />
        <span>Reset</span>
      </button>
    </div>
  );
};
