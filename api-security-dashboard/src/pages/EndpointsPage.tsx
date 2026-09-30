import React, { useEffect, useState, useCallback } from 'react';
import { MainLayout } from '../components/layout/MainLayout';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { fetchEndpoints, fetchApiMetrics } from '../api/endpoints';
import { EndpointSummary, ApiMetrics } from '../types/endpoint';
import { ApplicationApi } from '../api/applications';
import { Application } from '../types/application';
import { formatTimestamp } from '../utils/formatters';
import { 
  Network, 
  Search, 
  Filter, 
  Clock, 
  Activity, 
  AlertTriangle,
  RotateCw
} from 'lucide-react';

export const EndpointsPage: React.FC = () => {
  const [endpoints, setEndpoints] = useState<EndpointSummary[]>([]);
  const [metrics, setMetrics] = useState<ApiMetrics | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [selectedApp, setSelectedApp] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const [epData, metricsData, appsData] = await Promise.all([
        fetchEndpoints(selectedApp || undefined),
        fetchApiMetrics(selectedApp || undefined),
        ApplicationApi.getAll(),
      ]);
      setEndpoints(epData);
      setMetrics(metricsData);
      setApplications(appsData);
    } catch (err) {
      console.error('Failed to load endpoints data', err);
    } finally {
      setLoading(false);
    }
  }, [selectedApp]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const filteredEndpoints = endpoints.filter((ep) => {
    const matchesSearch = ep.endpoint.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          ep.applicationId.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesSearch;
  });

  const getLatencyBadge = (ms: number) => {
    if (ms < 50) return 'text-emerald-700 bg-emerald-50 border-emerald-200';
    if (ms < 150) return 'text-sky-700 bg-sky-50 border-sky-200';
    if (ms < 300) return 'text-amber-700 bg-amber-50 border-amber-200';
    return 'text-rose-700 bg-rose-50 border-rose-200';
  };

  return (
    <MainLayout onRefresh={loadData}>
      {/* Title & Action Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center space-x-2">
            <Network className="text-brand-600" size={24} />
            <span>Discovered API Endpoints</span>
          </h1>
          <p className="text-xs text-slate-500 font-mono mt-0.5">
            Real-time catalog of all API routes, latency profiles, and request frequencies across connected applications
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={loadData}
            className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-colors"
          >
            <RotateCw size={13} className={loading ? 'animate-spin' : ''} />
            <span>Refresh Catalog</span>
          </button>
        </div>
      </div>

      {/* Metrics Stat Cards */}
      {metrics && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-slate-500 uppercase">Total Endpoints</span>
              <Network size={16} className="text-brand-500" />
            </div>
            <p className="text-2xl font-bold font-mono text-slate-900 mt-2">{metrics.totalEndpoints}</p>
            <p className="text-[11px] text-slate-400 mt-1">Observed in real traffic</p>
          </div>

          <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-slate-500 uppercase">Total Calls</span>
              <Activity size={16} className="text-sky-500" />
            </div>
            <p className="text-2xl font-bold font-mono text-slate-900 mt-2">{metrics.totalRequests}</p>
            <p className="text-[11px] text-slate-400 mt-1">Captured across applications</p>
          </div>

          <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-slate-500 uppercase">Avg Response Time</span>
              <Clock size={16} className="text-amber-500" />
            </div>
            <p className="text-2xl font-bold font-mono text-slate-900 mt-2">{metrics.avgLatencyMs} ms</p>
            <p className="text-[11px] text-slate-400 mt-1">Global average latency</p>
          </div>

          <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-slate-500 uppercase">Error Rate</span>
              <AlertTriangle size={16} className="text-rose-500" />
            </div>
            <p className="text-2xl font-bold font-mono text-rose-600 mt-2">{metrics.errorRate}%</p>
            <p className="text-[11px] text-slate-400 mt-1">{metrics.errorCount} total HTTP errors (4xx/5xx)</p>
          </div>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div className="bg-white p-3.5 rounded-xl border border-slate-200 flex flex-col md:flex-row items-center justify-between gap-3 shadow-xs">
        <div className="relative flex-1 w-full">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search API endpoints or applications..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-1.5 text-xs font-mono bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-brand-500 focus:bg-white"
          />
        </div>

        <div className="flex items-center space-x-2 w-full md:w-auto">
          <Filter size={15} className="text-slate-400" />
          <select
            value={selectedApp}
            onChange={(e) => setSelectedApp(e.target.value)}
            className="text-xs font-mono bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5 text-slate-700 focus:outline-none focus:border-brand-500"
          >
            <option value="">All Applications</option>
            {applications.map((app) => (
              <option key={app.id} value={app.applicationId}>
                {app.name} ({app.applicationId})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Endpoints Table */}
      {loading ? (
        <LoadingSpinner message="Scanning active API endpoint catalog..." />
      ) : filteredEndpoints.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 p-12 text-center text-slate-400 font-mono text-sm space-y-2">
          <p className="text-slate-600 font-semibold">No endpoints discovered yet.</p>
          <p className="text-xs text-slate-400">
            Connect any backend application and send API requests to view live endpoint discovery.
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs font-mono">
              <thead>
                <tr className="bg-slate-100/80 border-b border-slate-200 text-slate-700 font-semibold">
                  <th className="py-3 px-4">Endpoint Path</th>
                  <th className="py-3 px-4">Application</th>
                  <th className="py-3 px-4">Methods</th>
                  <th className="py-3 px-4 text-center">Requests</th>
                  <th className="py-3 px-4">Avg Latency</th>
                  <th className="py-3 px-4">Min / Max</th>
                  <th className="py-3 px-4">Error Rate</th>
                  <th className="py-3 px-4">Last Activity</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredEndpoints.map((ep, idx) => (
                  <tr key={`${ep.applicationId}-${ep.endpoint}-${idx}`} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-semibold text-slate-900 flex items-center space-x-2">
                      <span className="truncate max-w-xs">{ep.endpoint}</span>
                    </td>
                    <td className="py-3 px-4 font-bold text-slate-800">{ep.applicationId}</td>
                    <td className="py-3 px-4">
                      <div className="flex items-center space-x-1">
                        {ep.methods.map((m) => (
                          <span
                            key={m}
                            className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                              m === 'POST' ? 'bg-indigo-50 text-indigo-700' :
                              m === 'GET' ? 'bg-sky-50 text-sky-700' :
                              m === 'DELETE' ? 'bg-rose-50 text-rose-700' :
                              m === 'PUT' ? 'bg-amber-50 text-amber-700' : 'bg-slate-100 text-slate-700'
                            }`}
                          >
                            {m}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-3 px-4 text-center font-bold text-slate-800">{ep.totalRequests}</td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded font-bold border ${getLatencyBadge(ep.avgLatencyMs)}`}>
                        {ep.avgLatencyMs} ms
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-500">
                      {ep.minLatencyMs} / {ep.maxLatencyMs} ms
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center space-x-2">
                        <span className={`font-bold ${ep.errorRate > 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                          {ep.errorRate}%
                        </span>
                        {ep.errorCount > 0 && (
                          <span className="text-[10px] text-slate-400">({ep.errorCount})</span>
                        )}
                      </div>
                    </td>
                    <td className="py-3 px-4 text-slate-500">
                      {ep.lastSeenAt ? formatTimestamp(ep.lastSeenAt) : 'Recent'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </MainLayout>
  );
};
