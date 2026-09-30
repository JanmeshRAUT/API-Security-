import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MainLayout } from '../components/layout/MainLayout';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { StatusBadge } from '../components/common/StatusBadge';
import { ThreatTable } from '../components/threats/ThreatTable';
import { ConnectAppGuideModal } from '../components/applications/ConnectAppGuideModal';
import { ApplicationApi } from '../api/applications';
import { EventApi } from '../api/events';
import { ThreatApi } from '../api/threats';
import { Application } from '../types/application';
import { SecurityEvent } from '../types/event';
import { Threat } from '../types/threat';
import { formatTimestamp, formatRelativeTime } from '../utils/formatters';
import { securitySocketManager } from '../websocket/securitySocket';
import { AppWindow, ArrowLeft, Activity, ShieldAlert, BookOpen, Trash2, Key, Copy, Check, RefreshCw } from 'lucide-react';

const METHOD_COLORS: Record<string, string> = {
  GET:    'bg-sky-50 text-sky-700 border-sky-200',
  POST:   'bg-violet-50 text-violet-700 border-violet-200',
  PUT:    'bg-amber-50 text-amber-700 border-amber-200',
  PATCH:  'bg-orange-50 text-orange-700 border-orange-200',
  DELETE: 'bg-red-50 text-red-700 border-red-200',
};

const statusColor = (code: number) => {
  if (code >= 200 && code < 300) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (code >= 400 && code < 500) return 'bg-amber-50 text-amber-700 border-amber-200';
  if (code >= 500)               return 'bg-red-50 text-red-700 border-red-200';
  return 'bg-slate-50 text-slate-600 border-slate-200';
};

export const ApplicationDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [app, setApp] = useState<Application | null>(null);
  const [events, setEvents] = useState<SecurityEvent[]>([]);
  const [threats, setThreats] = useState<Threat[]>([]);
  const [loading, setLoading] = useState(true);
  const [isGuideOpen, setIsGuideOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  
  // API Key state
  const [showApiKey, setShowApiKey] = useState(false);
  const [copiedKey, setCopiedKey] = useState(false);
  const [apiKey, setApiKey] = useState<string>('');
  
  // AppData for guide
  const [guideApp, setGuideApp] = useState<any>(null);

  const handleDeleteApp = async () => {
    if (!app || !window.confirm(`Are you sure you want to delete application "${app.name}" (${app.applicationId})?`)) {
      return;
    }
    try {
      setDeleting(true);
      await ApplicationApi.delete(app.id);
      navigate('/applications');
    } catch (err) {
      console.error('Failed to delete application', err);
      alert('Failed to delete application: ' + (err as Error).message);
    } finally {
      setDeleting(false);
    }
  };

  const fetchAppData = useCallback(async () => {
    if (!id) return;
    try {
      setLoading(true);
      const appData = await ApplicationApi.getById(id);
      setApp(appData);
      setApiKey((appData as any).apiKey || 'api_key_' + appData.applicationId.replace(/-/g, '_') + '_sec');

      const [eventsData, threatsData] = await Promise.all([
        EventApi.getByApplicationId(appData.applicationId),
        ThreatApi.getByApplicationId(appData.applicationId),
      ]);

      setEvents(eventsData);
      setThreats(threatsData);
    } catch (err) {
      console.error('Failed to load application details', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchAppData();
  }, [fetchAppData]);

  // Subscribe to real-time events for this specific application
  useEffect(() => {
    if (!app?.applicationId) return;
    const unsubscribe = securitySocketManager.onAppEvent(app.applicationId, (newEvent) => {
      setEvents((prev) => [newEvent, ...prev]);
    });
    return () => unsubscribe();
  }, [app?.applicationId]);

  if (loading) {
    return (
      <MainLayout>
        <LoadingSpinner message="Loading application details..." />
      </MainLayout>
    );
  }

  if (!app) {
    return (
      <MainLayout>
        <div className="panel p-12 text-center text-slate-500">
          Application not found with ID: {id}
        </div>
      </MainLayout>
    );
  }

  const mask = (key: string) => key.slice(0, 8) + '••••••••••••••••••••' + key.slice(-4);

  return (
    <MainLayout onRefresh={fetchAppData} pageCrumbs={['SOC Console', 'Applications', app.applicationId]}>
      {/* Navigation Header */}
      <div className="flex items-center gap-3 shrink-0">
        <button
          onClick={() => navigate('/applications')}
          className="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-md transition-colors"
        >
          <ArrowLeft size={16} />
        </button>
        <div>
          <h1 className="text-lg font-semibold text-slate-900">{app.name}</h1>
          <p className="text-sm text-slate-500 font-mono mt-0.5">{app.applicationId}</p>
        </div>
      </div>

      {/* App Overview Panel */}
      <div className="panel p-5 shrink-0">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-blue-50 rounded-lg">
              <AppWindow size={24} className="text-blue-600" />
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h2 className="text-base font-bold text-slate-900">{app.name}</h2>
                <StatusBadge status={app.status as any} />
              </div>
              <div className="flex items-center gap-3 mt-1 text-xs text-slate-500 font-mono">
                <span>Registered: <strong className="text-slate-700">{formatTimestamp(app.createdAt)}</strong></span>
                <span className="text-slate-300">•</span>
                <span>Last Active: <strong className="text-slate-700">{formatRelativeTime(app.lastSeenAt)}</strong></span>
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                setGuideApp({ ...app, apiKey: apiKey });
                setIsGuideOpen(true);
              }}
              className="btn-secondary"
            >
              <BookOpen size={14} />
              Setup Guide
            </button>
            <button
              onClick={handleDeleteApp}
              disabled={deleting}
              className="btn-secondary text-red-600 hover:bg-red-50 hover:text-red-700 border-transparent hover:border-red-200"
              title="Delete Application"
            >
              <Trash2 size={14} />
              {deleting ? 'Deleting...' : 'Delete'}
            </button>
          </div>
        </div>

        {app.description && (
          <p className="mt-4 text-sm text-slate-600">{app.description}</p>
        )}
        
        {/* API Key Section */}
        <div className="mt-5 p-4 bg-slate-50 rounded-lg border border-slate-200 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1.5">
              <Key size={13} className="text-slate-500" />
              <p className="text-xs font-semibold text-slate-700 uppercase tracking-wide">Ingestion API Key</p>
            </div>
            <div className="flex items-center gap-2">
              <code className="text-sm font-mono text-slate-800 bg-white px-2 py-1 rounded border border-slate-200 select-all">
                {showApiKey ? apiKey : mask(apiKey)}
              </code>
              <button
                onClick={() => setShowApiKey(!showApiKey)}
                className="text-xs font-medium text-blue-600 hover:text-blue-800 underline ml-1"
              >
                {showApiKey ? 'Hide' : 'Show'}
              </button>
            </div>
          </div>
          
          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={() => {
                navigator.clipboard.writeText(apiKey);
                setCopiedKey(true);
                setTimeout(() => setCopiedKey(false), 2000);
              }}
              className="btn-secondary py-1.5 px-3"
            >
              {copiedKey ? <Check size={13} className="text-emerald-600" /> : <Copy size={13} />}
              {copiedKey ? 'Copied' : 'Copy'}
            </button>
            <button
              onClick={async () => {
                if (window.confirm('Regenerating this API Key will immediately revoke the previous key. Continue?')) {
                  try {
                    const rotateRes = await ApplicationApi.rotateKey(app.id);
                    setApiKey(rotateRes.apiKey);
                    setShowApiKey(true);
                    setGuideApp(rotateRes);
                    setIsGuideOpen(true);
                  } catch (err) {
                    console.error('Failed to rotate API Key', err);
                    alert('Failed to rotate API Key. Please ensure backend is updated.');
                  }
                }
              }}
              className="btn-secondary py-1.5 px-3 text-amber-700 hover:bg-amber-50 hover:border-amber-200"
            >
              <RefreshCw size={13} />
              Rotate
            </button>
          </div>
        </div>
      </div>


      {/* Guided Connection Modal */}
      <ConnectAppGuideModal
        isOpen={isGuideOpen}
        appData={guideApp || app}
        onClose={() => setIsGuideOpen(false)}
      />

      {/* Real-Time Activity & Threats */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 flex-1 min-h-0">
        
        {/* Recent Traffic Stream */}
        <div className="panel flex flex-col h-full overflow-hidden">
          <div className="panel-header shrink-0">
            <div className="flex items-center gap-2">
              <Activity size={14} className="text-blue-500 animate-pulse" />
              <span className="panel-title">Live App API Events</span>
            </div>
            <span className="status-pill status-neutral">{events.length} events</span>
          </div>

          <div className="flex-1 overflow-auto">
            {events.length === 0 ? (
              <div className="empty-state">
                <Activity size={22} className="text-slate-300" />
                <p>No recent API events recorded for this application.</p>
              </div>
            ) : (
              <table className="soc-table">
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Method</th>
                    <th>Endpoint</th>
                    <th>Status</th>
                    <th>Latency</th>
                  </tr>
                </thead>
                <tbody>
                  {events.map((evt) => (
                    <tr key={evt.id}>
                      <td className="font-mono text-xs text-slate-500 whitespace-nowrap">{formatTimestamp(evt.timestamp)}</td>
                      <td>
                        <span className={`status-pill border ${METHOD_COLORS[evt.httpMethod] ?? 'bg-slate-50 text-slate-600 border-slate-200'}`}>
                          {evt.httpMethod}
                        </span>
                      </td>
                      <td className="font-mono font-medium text-slate-800 truncate max-w-[160px]">{evt.endpoint}</td>
                      <td>
                        <span className={`status-pill border ${statusColor(evt.statusCode)}`}>
                          {evt.statusCode}
                        </span>
                      </td>
                      <td className="font-mono text-slate-600">{evt.responseTimeMs} ms</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Application Threat Incidents */}
        <div className="panel flex flex-col h-full overflow-hidden">
          <div className="panel-header shrink-0">
            <div className="flex items-center gap-2">
              <ShieldAlert size={14} className="text-amber-500" />
              <span className="panel-title">Associated Threat Incidents</span>
            </div>
            <span className="status-pill status-neutral">{threats.length} threats</span>
          </div>

          <div className="flex-1 overflow-auto">
            {threats.length === 0 ? (
              <div className="empty-state">
                <ShieldAlert size={22} className="text-slate-300" />
                <p>No threat incidents detected for this application.</p>
              </div>
            ) : (
              <ThreatTable threats={threats} />
            )}
          </div>
        </div>
      </div>
    </MainLayout>
  );
};
