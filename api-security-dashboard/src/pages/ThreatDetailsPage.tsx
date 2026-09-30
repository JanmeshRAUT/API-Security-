import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MainLayout } from '../components/layout/MainLayout';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { SeverityBadge } from '../components/common/SeverityBadge';
import { StatusBadge } from '../components/common/StatusBadge';
import { RiskGauge } from '../components/common/RiskGauge';
import { ThreatApi } from '../api/threats';
import { Threat, ThreatStatus } from '../types/threat';
import { formatTimestamp, getThreatTypeLabel } from '../utils/formatters';
import { ArrowLeft, ShieldAlert, CheckCircle2, Eye, AlertTriangle } from 'lucide-react';

export const ThreatDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [threat, setThreat] = useState<Threat | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  const fetchThreat = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      const data = await ThreatApi.getById(id);
      setThreat(data);
    } catch (err) {
      console.error('Failed to load threat details', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchThreat();
  }, [fetchThreat]);

  const handleUpdateStatus = async (newStatus: ThreatStatus) => {
    if (!threat || !id) return;
    try {
      setUpdating(true);
      const updated = await ThreatApi.updateStatus(id, newStatus);
      setThreat(updated);
    } catch (err) {
      console.error('Failed to update threat status', err);
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <MainLayout>
        <LoadingSpinner message="Loading threat incident details..." />
      </MainLayout>
    );
  }

  if (!threat) {
    return (
      <MainLayout>
        <div className="panel p-12 text-center text-slate-500">
          Threat record not found for ID: {id}
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout onRefresh={fetchThreat} pageCrumbs={['SOC Console', 'Threats', threat.id]}>
      {/* Navigation Header */}
      <div className="flex items-center gap-3 shrink-0">
        <button
          onClick={() => navigate('/threats')}
          className="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-md transition-colors"
        >
          <ArrowLeft size={16} />
        </button>
        <div>
          <h1 className="text-lg font-semibold text-slate-900">Incident Details</h1>
          <p className="text-sm text-slate-500 font-mono mt-0.5">{threat.id}</p>
        </div>
      </div>

      {/* Main Incident Panel */}
      <div className="panel p-6 shrink-0">
        <div className="flex flex-wrap items-start justify-between gap-4 pb-5 border-b border-slate-100">
          <div className="flex items-start gap-4">
            <div className={`p-3 rounded-lg ${
              threat.severity === 'CRITICAL' ? 'bg-rose-50 text-rose-600' : 'bg-amber-50 text-amber-600'
            }`}>
              <ShieldAlert size={28} />
            </div>
            <div>
              <div className="flex items-center gap-3">
                <SeverityBadge severity={threat.severity} size="lg" />
                <StatusBadge status={threat.status} />
              </div>
              <h1 className="text-xl font-bold text-slate-900 mt-2">
                {getThreatTypeLabel(threat.threatType)}
              </h1>
              <p className="text-xs font-mono text-slate-500 mt-1.5">
                Detected: <strong className="text-slate-700">{formatTimestamp(threat.detectedAt)}</strong>
              </p>
            </div>
          </div>

          {/* Status Action Buttons */}
          <div className="flex items-center gap-2">
            <button
              disabled={updating || threat.status === 'ACKNOWLEDGED'}
              onClick={() => handleUpdateStatus('ACKNOWLEDGED')}
              className="btn-secondary text-amber-700 hover:bg-amber-50 hover:border-amber-200 disabled:opacity-50"
            >
              <Eye size={14} />
              Acknowledge
            </button>
            <button
              disabled={updating || threat.status === 'RESOLVED'}
              onClick={() => handleUpdateStatus('RESOLVED')}
              className="btn-secondary text-emerald-700 hover:bg-emerald-50 hover:border-emerald-200 disabled:opacity-50"
            >
              <CheckCircle2 size={14} />
              Mark Resolved
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 pt-5">
          {/* Left Column: Details */}
          <div className="space-y-6">
            <div>
              <h3 className="text-sm font-bold text-slate-900 mb-3 uppercase tracking-wide">Threat Context</h3>
              <table className="w-full text-left text-sm">
                <tbody className="divide-y divide-slate-50">
                  <tr>
                    <td className="py-2 text-slate-500 font-semibold w-1/3">Target App</td>
                    <td className="py-2 font-mono font-bold text-blue-600">{threat.applicationId}</td>
                  </tr>
                  <tr>
                    <td className="py-2 text-slate-500 font-semibold">Endpoint</td>
                    <td className="py-2 font-mono text-slate-800 break-all">{threat.httpMethod} {threat.endpoint}</td>
                  </tr>
                  <tr>
                    <td className="py-2 text-slate-500 font-semibold">Client IP</td>
                    <td className="py-2 font-mono text-slate-800">{threat.clientIp || 'Unknown'}</td>
                  </tr>
                  <tr>
                    <td className="py-2 text-slate-500 font-semibold">User Identity</td>
                    <td className="py-2 font-mono text-slate-800">{threat.userId || 'Anonymous'}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            {threat.description && (
              <div>
                <h3 className="text-sm font-bold text-slate-900 mb-2 uppercase tracking-wide">AI Analysis</h3>
                <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg text-sm text-slate-700 leading-relaxed">
                  {threat.description}
                </div>
              </div>
            )}
          </div>

          {/* Right Column: AI Risk Scoring */}
          <div className="space-y-6 md:pl-8 md:border-l border-slate-100">
            <div>
              <h3 className="text-sm font-bold text-slate-900 mb-4 uppercase tracking-wide">AI Risk Assessment</h3>
              <div className="flex flex-col items-center justify-center p-6 bg-slate-50 border border-slate-200 rounded-lg">
                <RiskGauge score={threat.riskScore} />
                <p className="mt-4 text-xs font-mono text-slate-500 text-center max-w-[250px]">
                  Risk calculated based on behavioral anomalies, payload signatures, and historical baseline deviations.
                </p>
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wide">Detection Confidence</h3>
                <span className="font-mono font-bold text-slate-800">{(threat.confidence * 100).toFixed(1)}%</span>
              </div>
              <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                <div 
                  className={`h-full ${threat.confidence > 0.8 ? 'bg-emerald-500' : 'bg-amber-500'}`}
                  style={{ width: `${threat.confidence * 100}%` }}
                />
              </div>
              <p className="text-xs text-slate-500 mt-2 flex items-start gap-1.5">
                <AlertTriangle size={14} className="text-slate-400 shrink-0 mt-0.5" />
                <span>
                  High confidence indicates the ML model has strongly correlated this event sequence with known attack patterns.
                </span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};
