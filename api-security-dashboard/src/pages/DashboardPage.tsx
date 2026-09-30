import React, { useEffect, useState, useCallback } from 'react';
import { MainLayout } from '../components/layout/MainLayout';
import { KpiCards } from '../components/dashboard/KpiCards';
import { ThreatBreakdownChart } from '../components/dashboard/ThreatBreakdownChart';
import { SeverityChart } from '../components/dashboard/SeverityChart';
import { LiveThreatFeed } from '../components/dashboard/LiveThreatFeed';
import { LiveTrafficFeed } from '../components/dashboard/LiveTrafficFeed';
import { DashboardApi } from '../api/dashboard';
import { DashboardSummary } from '../types/dashboard';
import { ShieldCheck, PieChart, BarChart3 } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchSummary = useCallback(async (isManualRefresh = false) => {
    try {
      if (isManualRefresh) setRefreshing(true);
      const data = await DashboardApi.getSummary();
      setSummary(data);
    } catch (err) {
      console.error('Failed to load dashboard summary', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchSummary(); }, [fetchSummary]);

  return (
    <MainLayout
      onRefresh={() => fetchSummary(true)}
      isRefreshing={refreshing}
      pageCrumbs={['SOC Console', 'Overview']}
      fillHeight
    >
      {/* ── Page title ── */}
      <div className="flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-lg font-semibold text-slate-900">Security Overview</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Real-time API threat intelligence and thread anomaly monitoring
          </p>
        </div>
        <div className="flex items-center gap-2 text-sm font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 px-3 py-1.5 rounded-md">
          <ShieldCheck size={14} className="text-emerald-600" />
          <span>Protection Active</span>
        </div>
      </div>

      {/* ── KPI Row ── */}
      <div className="shrink-0">
        <KpiCards summary={summary} loading={loading} />
      </div>

      {/* ── Live Feeds — grow to fill space ── */}
      <div className="grid grid-cols-2 gap-4 flex-1 min-h-0">
        <LiveThreatFeed />
        <LiveTrafficFeed />
      </div>

      {/* ── Analytics Charts — grow to fill space ── */}
      <div className="grid grid-cols-2 gap-4 flex-1 min-h-0">
        <div className="panel flex flex-col">
          <div className="panel-header shrink-0">
            <div className="flex items-center gap-2">
              <PieChart size={13} className="text-slate-400" />
              <span className="panel-title">Threat Categories</span>
            </div>
            <span className="text-xs text-slate-400 font-mono">AI Classifier</span>
          </div>
          <div className="flex-1 p-4 min-h-0">
            <ThreatBreakdownChart summary={summary} />
          </div>
        </div>

        <div className="panel flex flex-col">
          <div className="panel-header shrink-0">
            <div className="flex items-center gap-2">
              <BarChart3 size={13} className="text-slate-400" />
              <span className="panel-title">Severity Distribution</span>
            </div>
            <span className="text-xs text-slate-400 font-mono">Risk Matrix</span>
          </div>
          <div className="flex-1 p-4 min-h-0">
            <SeverityChart summary={summary} />
          </div>
        </div>
      </div>
    </MainLayout>
  );
};
