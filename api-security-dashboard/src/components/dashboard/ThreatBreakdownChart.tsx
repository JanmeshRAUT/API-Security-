import React from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { DashboardSummary } from '../../types/dashboard';
import { getThreatTypeLabel } from '../../utils/formatters';

interface ThreatBreakdownChartProps {
  summary: DashboardSummary | null;
}

const COLORS = ['#2563eb', '#d97706', '#dc2626', '#16a34a', '#7c3aed'];

export const ThreatBreakdownChart: React.FC<ThreatBreakdownChartProps> = ({ summary }) => {
  const data = React.useMemo(() => {
    if (!summary || !summary.threatsByType) return [];
    return Object.entries(summary.threatsByType).map(([type, count]) => ({
      name: getThreatTypeLabel(type),
      value: count,
    }));
  }, [summary]);

  if (data.length === 0) {
    return (
      <div className="empty-state h-full">
        <p>No threat breakdown data available</p>
      </div>
    );
  }

  return (
    <div className="h-full w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={38}
            outerRadius={55}
            paddingAngle={3}
            dataKey="value"
          >
            {data.map((_, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
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
          />
          <Legend
            wrapperStyle={{ fontSize: '10px', fontFamily: '"IBM Plex Mono", monospace' }}
            formatter={(value) => <span style={{ color: '#64748b' }}>{value}</span>}
            iconSize={8}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};
