import React from 'react';
import { DashboardSummary } from '../../types/dashboard';
import { AppWindow, Activity, ShieldAlert, AlertTriangle } from 'lucide-react';

interface KpiCardsProps {
  summary: DashboardSummary | null;
  loading?: boolean;
}

const cards = (summary: DashboardSummary | null, loading: boolean) => [
  {
    id: 'total-applications',
    label: 'Applications',
    value: summary?.totalApplications ?? 0,
    sub: 'Registered services',
    icon: AppWindow,
    iconColor: 'text-blue-600',
    iconBg: 'bg-blue-50',
    accent: 'bg-blue-600',
    loading,
  },
  {
    id: 'total-events',
    label: 'API Events',
    value: summary?.totalEvents ?? 0,
    sub: 'Total ingested',
    icon: Activity,
    iconColor: 'text-violet-600',
    iconBg: 'bg-violet-50',
    accent: 'bg-violet-500',
    loading,
  },
  {
    id: 'detected-threats',
    label: 'Threats Detected',
    value: summary?.totalThreats ?? 0,
    sub: 'All severities',
    icon: ShieldAlert,
    iconColor: 'text-amber-600',
    iconBg: 'bg-amber-50',
    accent: 'bg-amber-500',
    loading,
  },
  {
    id: 'critical-threats',
    label: 'Critical Threats',
    value: summary?.criticalThreats ?? 0,
    sub: summary?.criticalThreats && summary.criticalThreats > 0
      ? 'Action required'
      : 'No critical incidents',
    icon: AlertTriangle,
    iconColor: summary?.criticalThreats && summary.criticalThreats > 0 ? 'text-red-600' : 'text-emerald-600',
    iconBg: summary?.criticalThreats && summary.criticalThreats > 0 ? 'bg-red-50' : 'bg-emerald-50',
    accent: summary?.criticalThreats && summary.criticalThreats > 0 ? 'bg-red-600' : 'bg-emerald-500',
    loading,
  },
];

export const KpiCards: React.FC<KpiCardsProps> = ({ summary, loading = false }) => {
  return (
    <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
      {cards(summary, loading).map((card) => {
        const Icon = card.icon;
        return (
          <div key={card.id} id={`kpi-${card.id}`} className="kpi-card">
            <div className={`kpi-card-accent ${card.accent}`} />
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold text-slate-400 uppercase tracking-widest font-mono mb-1.5">
                  {card.label}
                </p>
                <p className="text-3xl font-bold text-slate-900 font-mono leading-none">
                  {card.loading
                    ? <span className="inline-block w-12 h-7 bg-slate-100 rounded animate-pulse" />
                    : card.value.toLocaleString()
                  }
                </p>
                <p className="text-sm text-slate-500 mt-1.5">{card.sub}</p>
              </div>
              <div className={`p-2.5 rounded-lg ${card.iconBg}`}>
                <Icon size={18} className={card.iconColor} />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};
