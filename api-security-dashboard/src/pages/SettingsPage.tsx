import React, { useState, useEffect } from 'react';
import { MainLayout } from '../components/layout/MainLayout';
import { Server, Brain, Zap, Shield, Webhook, Database, Settings2, Mail, Save } from 'lucide-react';
import { ConnectionBadge } from '../components/layout/ConnectionBadge';
import { SettingsApi, Settings } from '../api/settings';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

type SettingsTab = 'general' | 'ai-engine' | 'integrations' | 'advanced';

export const SettingsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<SettingsTab>('general');
  const [settings, setSettings] = useState<Settings | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      setLoading(true);
      const data = await SettingsApi.get();
      setSettings(data);
    } catch (err) {
      console.error('Failed to load settings', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (updates: Partial<Settings>) => {
    if (!settings) return;
    try {
      setSaving(true);
      const updated = await SettingsApi.update(updates);
      setSettings(updated);
    } catch (err) {
      console.error('Failed to update settings', err);
      alert('Failed to save settings. Make sure backend is running.');
    } finally {
      setSaving(false);
    }
  };

  if (loading || !settings) {
    return (
      <MainLayout>
        <LoadingSpinner message="Loading Platform Settings..." />
      </MainLayout>
    );
  }

  const tabs = [
    { id: 'general', label: 'General System', icon: <Settings2 size={16} /> },
    { id: 'ai-engine', label: 'AI & Threat Engine', icon: <Brain size={16} /> },
    { id: 'integrations', label: 'Integrations & Alerts', icon: <Webhook size={16} /> },
    { id: 'advanced', label: 'Advanced Tuning', icon: <Zap size={16} /> },
  ] as const;

  return (
    <MainLayout pageCrumbs={['SOC Console', 'Platform Settings']}>
      <div className="flex items-center justify-between shrink-0 mb-2">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Platform Settings</h1>
          <p className="text-sm text-slate-500 mt-1">
            Configure global SOC parameters, AI models, and real-time integrations.
          </p>
        </div>
        {saving && (
          <div className="flex items-center gap-2 text-sm text-slate-500">
            <Save size={14} className="animate-pulse" /> Saving...
          </div>
        )}
      </div>

      <div className="flex flex-col md:flex-row gap-6 mt-4">
        {/* Sidebar Tabs */}
        <div className="w-full md:w-64 shrink-0 space-y-1">
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as SettingsTab)}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition-colors ${
                activeTab === tab.id
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`}
            >
              <div className={`${activeTab === tab.id ? 'text-blue-100' : 'text-slate-400'}`}>
                {tab.icon}
              </div>
              {tab.label}
            </button>
          ))}
        </div>

        {/* Tab Content */}
        <div className="flex-1 space-y-6 max-w-4xl">
          
          {/* GENERAL TAB */}
          {activeTab === 'general' && (
            <div className="animate-in fade-in slide-in-from-bottom-2 duration-300 space-y-6">
              <div className="panel">
                <div className="panel-header">
                  <div className="flex items-center gap-2">
                    <Server size={14} className="text-slate-400" />
                    <span className="panel-title">System Connectivity</span>
                  </div>
                  <ConnectionBadge />
                </div>
                <div className="p-5">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg">
                      <p className="text-xs text-slate-500 font-mono uppercase tracking-widest mb-1.5">REST API Endpoint</p>
                      <code className="text-sm font-mono font-bold text-slate-900 select-all">http://localhost:8085/api/v1</code>
                    </div>
                    <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg">
                      <p className="text-xs text-slate-500 font-mono uppercase tracking-widest mb-1.5">WebSocket (STOMP)</p>
                      <code className="text-sm font-mono font-bold text-slate-900 select-all">ws://localhost:8085/ws/security</code>
                    </div>
                  </div>
                </div>
              </div>

              <div className="panel">
                <div className="panel-header">
                  <div className="flex items-center gap-2">
                    <Database size={14} className="text-slate-400" />
                    <span className="panel-title">Data Retention Policy</span>
                  </div>
                </div>
                <div className="p-5 space-y-5">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-sm font-semibold text-slate-800">Raw API Traffic Retention</h4>
                      <p className="text-xs text-slate-500 mt-0.5">How long to keep non-malicious event payloads.</p>
                    </div>
                    <select 
                      value={settings.dataRetentionDays}
                      onChange={(e) => handleUpdate({ dataRetentionDays: parseInt(e.target.value) })}
                      className="soc-input w-40 cursor-pointer text-sm"
                    >
                      <option value={7}>7 Days</option>
                      <option value={14}>14 Days</option>
                      <option value={30}>30 Days</option>
                    </select>
                  </div>
                  <div className="border-t border-slate-100 pt-4 flex items-center justify-between">
                    <div>
                      <h4 className="text-sm font-semibold text-slate-800">Threat Incident Logs</h4>
                      <p className="text-xs text-slate-500 mt-0.5">Permanent storage policy for security incidents.</p>
                    </div>
                    <select 
                      value={settings.incidentRetentionDays}
                      onChange={(e) => handleUpdate({ incidentRetentionDays: parseInt(e.target.value) })}
                      className="soc-input w-40 cursor-pointer text-sm"
                    >
                      <option value={180}>6 Months</option>
                      <option value={365}>1 Year</option>
                      <option value={1095}>3 Years</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* AI ENGINE TAB */}
          {activeTab === 'ai-engine' && (
            <div className="animate-in fade-in slide-in-from-bottom-2 duration-300 space-y-6">
              <div className="panel">
                <div className="panel-header">
                  <div className="flex items-center gap-2">
                    <Brain size={14} className="text-slate-400" />
                    <span className="panel-title">Anomaly Detection Sensitivity</span>
                  </div>
                </div>
                <div className="p-5 space-y-5">
                  <p className="text-sm text-slate-600">
                    Adjust how strictly the AI model flags deviations from historical API baselines. Higher sensitivity increases alerts but may cause false positives.
                  </p>
                  <div className="space-y-2">
                    <div className="flex justify-between text-xs font-bold text-slate-700">
                      <span>Low (Lenient)</span>
                      <span>Medium (Balanced)</span>
                      <span>High (Strict)</span>
                    </div>
                    <input 
                      type="range" 
                      min="1" 
                      max="3" 
                      value={settings.anomalySensitivity === 'LOW' ? 1 : settings.anomalySensitivity === 'MEDIUM' ? 2 : 3}
                      onChange={(e) => {
                        const val = parseInt(e.target.value);
                        const mapped = val === 1 ? 'LOW' : val === 2 ? 'MEDIUM' : 'HIGH';
                        handleUpdate({ anomalySensitivity: mapped });
                      }}
                      className="w-full accent-blue-600 cursor-pointer" 
                    />
                  </div>
                </div>
              </div>

              <div className="panel">
                <div className="panel-header">
                  <div className="flex items-center gap-2">
                    <Shield size={14} className="text-slate-400" />
                    <span className="panel-title">Automated Responses (WAF Sync)</span>
                  </div>
                </div>
                <div className="p-5 space-y-4">
                  <label className="flex items-start gap-3 cursor-pointer p-3 rounded-lg hover:bg-slate-50 transition-colors">
                    <input 
                      type="checkbox" 
                      checked={settings.autoBanEnabled}
                      onChange={(e) => handleUpdate({ autoBanEnabled: e.target.checked })}
                      className="mt-1 w-4 h-4 text-blue-600 rounded border-slate-300 focus:ring-blue-600 cursor-pointer" 
                    />
                    <div>
                      <h4 className="text-sm font-semibold text-slate-800">Auto-Ban Critical Threats</h4>
                      <p className="text-xs text-slate-500 mt-0.5">Automatically ban IPs via API Gateway when a threat score exceeds 95.</p>
                    </div>
                  </label>
                  <label className="flex items-start gap-3 cursor-pointer p-3 rounded-lg hover:bg-slate-50 transition-colors">
                    <input 
                      type="checkbox" 
                      checked={settings.rateLimitSuspicious}
                      onChange={(e) => handleUpdate({ rateLimitSuspicious: e.target.checked })}
                      className="mt-1 w-4 h-4 text-blue-600 rounded border-slate-300 focus:ring-blue-600 cursor-pointer" 
                    />
                    <div>
                      <h4 className="text-sm font-semibold text-slate-800">Rate Limit Suspicious Actors</h4>
                      <p className="text-xs text-slate-500 mt-0.5">Dynamically inject rate-limits for sessions with medium risk scores (70-94).</p>
                    </div>
                  </label>
                </div>
              </div>
            </div>
          )}

          {/* INTEGRATIONS TAB */}
          {activeTab === 'integrations' && (
            <div className="animate-in fade-in slide-in-from-bottom-2 duration-300 space-y-6">
              <div className="panel">
                <div className="panel-header">
                  <div className="flex items-center gap-2">
                    <Webhook size={14} className="text-slate-400" />
                    <span className="panel-title">Webhooks & Alerting Channels</span>
                  </div>
                </div>
                <div className="divide-y divide-slate-100">
                  <div className="p-4 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-slate-100 rounded-md">
                        <Mail size={16} className="text-slate-700" />
                      </div>
                      <div>
                        <h4 className="text-sm font-semibold text-slate-800">Security Team Email Group</h4>
                        <input 
                          type="email" 
                          value={settings.alertsEmail}
                          onChange={(e) => setSettings({ ...settings, alertsEmail: e.target.value })}
                          onBlur={(e) => handleUpdate({ alertsEmail: e.target.value })}
                          className="text-xs font-mono text-slate-500 mt-1 bg-transparent border-b border-slate-200 focus:border-blue-500 focus:outline-none px-1 py-0.5 w-64"
                        />
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="status-pill status-safe">Active</span>
                    </div>
                  </div>
                  <div className="p-4 flex flex-col gap-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className="p-2 bg-slate-100 rounded-md">
                          <svg className="w-4 h-4 text-slate-700" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M5.042 15.165a2.528 2.528 0 0 1-2.52 2.523A2.528 2.528 0 0 1 0 15.165a2.527 2.527 0 0 1 2.522-2.52h2.52v2.52zM6.313 15.165a2.527 2.527 0 0 1 2.521-2.52 2.527 2.527 0 0 1 2.521 2.52v5.04A2.528 2.528 0 0 1 8.834 22.7a2.528 2.528 0 0 1-2.521-2.522v-5.013zM8.834 8.86a2.528 2.528 0 0 1-2.521-2.52A2.528 2.528 0 0 1 8.834 3.818a2.527 2.527 0 0 1 2.521 2.521v2.52H8.834zM8.834 10.12h5.042a2.528 2.528 0 0 1 2.521 2.521 2.528 2.528 0 0 1-2.521 2.521H8.834a2.528 2.528 0 0 1-2.521-2.52 2.528 2.528 0 0 1 2.521-2.522zM18.956 8.86a2.528 2.528 0 0 1 2.522-2.52A2.528 2.528 0 0 1 24 8.86a2.528 2.528 0 0 1-2.522 2.521h-2.522V8.86zM17.688 8.86a2.528 2.528 0 0 1-2.523 2.52 2.527 2.527 0 0 1-2.52-2.52V3.819a2.528 2.528 0 0 1 2.52-2.522 2.528 2.528 0 0 1 2.523 2.522V8.86zM15.165 15.165a2.528 2.528 0 0 1 2.523 2.52 2.528 2.528 0 0 1-2.523 2.522a2.528 2.528 0 0 1-2.52-2.522v-2.52h2.52v2.52zM15.165 13.905h-5.042a2.528 2.528 0 0 1-2.522-2.521 2.528 2.528 0 0 1 2.522-2.521h5.042a2.528 2.528 0 0 1 2.523 2.521 2.528 2.528 0 0 1-2.523 2.521z"/>
                          </svg>
                        </div>
                        <div>
                          <h4 className="text-sm font-semibold text-slate-800">Slack Webhook</h4>
                          <p className="text-xs font-mono text-slate-500 mt-0.5">Triggers on severity = HIGH or CRITICAL</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <label className="relative inline-flex items-center cursor-pointer">
                          <input 
                            type="checkbox" 
                            checked={settings.slackAlertsEnabled}
                            onChange={(e) => handleUpdate({ slackAlertsEnabled: e.target.checked })}
                            className="sr-only peer" 
                          />
                          <div className="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-blue-600"></div>
                        </label>
                      </div>
                    </div>
                    {settings.slackAlertsEnabled && (
                      <div className="pl-11 pr-4 py-2">
                        <input 
                          type="text" 
                          value={settings.slackWebhookUrl}
                          onChange={(e) => setSettings({ ...settings, slackWebhookUrl: e.target.value })}
                          onBlur={(e) => handleUpdate({ slackWebhookUrl: e.target.value })}
                          placeholder="https://hooks.slack.com/services/..."
                          className="soc-input w-full text-xs font-mono"
                        />
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ADVANCED TAB */}
          {activeTab === 'advanced' && (
            <div className="animate-in fade-in slide-in-from-bottom-2 duration-300 space-y-6">
              <div className="panel border-rose-200">
                <div className="panel-header bg-rose-50/50">
                  <div className="flex items-center gap-2">
                    <Zap size={14} className="text-rose-600" />
                    <span className="panel-title text-rose-900">Danger Zone</span>
                  </div>
                </div>
                <div className="p-5 space-y-4">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                    <div>
                      <h4 className="text-sm font-semibold text-slate-800">Purge All Telemetry Data</h4>
                      <p className="text-xs text-slate-500 mt-0.5">Permanently deletes all events, keeping only incident records.</p>
                    </div>
                    <button className="btn-secondary text-rose-600 hover:bg-rose-50 hover:border-rose-200">Purge Data</button>
                  </div>
                  <div className="flex items-center justify-between pt-1">
                    <div>
                      <h4 className="text-sm font-semibold text-slate-800">Factory Reset Platform</h4>
                      <p className="text-xs text-slate-500 mt-0.5">Wipes all applications, keys, events, and incidents.</p>
                    </div>
                    <button className="btn-primary bg-rose-600 hover:bg-rose-700 text-white border-transparent">Factory Reset</button>
                  </div>
                </div>
              </div>
            </div>
          )}

        </div>
      </div>
    </MainLayout>
  );
};
