import React from 'react';
import { SecurityEventFilter } from '../../types/event';
import { Search, Filter, RotateCcw } from 'lucide-react';

interface TrafficFilterBarProps {
  filter: SecurityEventFilter;
  onChange: (updated: SecurityEventFilter) => void;
  onReset: () => void;
}

export const TrafficFilterBar: React.FC<TrafficFilterBarProps> = ({ filter, onChange, onReset }) => {
  return (
    <div className="panel flex flex-wrap items-center gap-3 px-4 py-3 shrink-0">
      <div className="flex items-center gap-2 text-xs font-semibold text-slate-400 uppercase tracking-widest font-mono">
        <Filter size={13} className="text-blue-500" />
        <span>Filters</span>
      </div>

      <select
        value={filter.httpMethod || ''}
        onChange={(e) => onChange({ ...filter, httpMethod: e.target.value || undefined, page: 0 })}
        className="soc-input py-1.5 text-sm w-auto"
      >
        <option value="">All Methods</option>
        <option value="GET">GET</option>
        <option value="POST">POST</option>
        <option value="PUT">PUT</option>
        <option value="DELETE">DELETE</option>
      </select>

      <div className="relative">
        <Search size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Filter by Application ID..."
          value={filter.applicationId || ''}
          onChange={(e) => onChange({ ...filter, applicationId: e.target.value || undefined, page: 0 })}
          className="soc-input pl-8 w-48"
        />
      </div>

      <div className="relative">
        <Search size={13} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Filter Endpoint..."
          value={filter.endpoint || ''}
          onChange={(e) => onChange({ ...filter, endpoint: e.target.value || undefined, page: 0 })}
          className="soc-input pl-8 w-48"
        />
      </div>

      <button onClick={onReset} className="ml-auto btn-secondary py-1.5">
        <RotateCcw size={12} />
        <span>Reset</span>
      </button>
    </div>
  );
};
