import React from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { DashboardSummary } from '../../types/dashboard';

interface SeverityChartProps {
  summary: DashboardSummary | null;
}

const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: '#dc2626',
  HIGH: '#d97706',
  MEDIUM: '#2563eb',
  LOW: '#64748b',
};

export const SeverityChart: React.FC<SeverityChartProps> = ({ summary }) => {
  const data = React.useMemo(() => {
    const defaultCounts = { LOW: 0, MEDIUM: 0, HIGH: 0, CRITICAL: 0 };
    const counts = { ...defaultCounts, ...(summary?.threatsBySeverity || {}) };
    return [
      { severity: 'LOW', count: counts.LOW },
      { severity: 'MED', count: counts.MEDIUM },
      { severity: 'HIGH', count: counts.HIGH },
      { severity: 'CRIT', count: counts.CRITICAL },
    ];
  }, [summary]);

  return (
    <div className="h-full w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 6, right: 6, left: -20, bottom: 0 }}>
          <XAxis
            dataKey="severity"
            tick={{ fill: '#94a3b8', fontSize: 9, fontFamily: '"IBM Plex Mono", monospace' }}
            axisLine={{ stroke: '#e2e8f0' }}
            tickLine={false}
          />
          <YAxis
            allowDecimals={false}
            tick={{ fill: '#94a3b8', fontSize: 9, fontFamily: '"IBM Plex Mono", monospace' }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            contentStyle={{
              background: '#1e293b',
              border: '1px solid #334155',
              borderRadius: '4px',
              fontSize: '11px',
              fontFamily: '"IBM Plex Mono", monospace',
              color: '#f1f5f9',
              padding: '4px 8px',
            }}
            cursor={{ fill: 'rgba(0,0,0,0.04)' }}
          />
          <Bar dataKey="count" radius={[3, 3, 0, 0]} maxBarSize={36}>
            {data.map((entry, index) => (
              <Cell key={`bar-${index}`} fill={SEVERITY_COLORS[entry.severity === 'MED' ? 'MEDIUM' : entry.severity === 'CRIT' ? 'CRITICAL' : entry.severity] || '#64748b'} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};
