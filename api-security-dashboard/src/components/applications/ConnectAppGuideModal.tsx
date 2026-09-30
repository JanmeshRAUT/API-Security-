import React, { useState, useEffect } from 'react';
import { 
  X, 
  Check, 
  Copy, 
  Radio, 
  CheckCircle2, 
  ArrowRight, 
  Layers
} from 'lucide-react';
import { ApplicationRegistrationResponse, Application } from '../../types/application';
import { SecurityEvent } from '../../types/event';
import { EventApi } from '../../api/events';
import { securitySocketManager } from '../../websocket/securitySocket';

interface ConnectAppGuideModalProps {
  appData?: ApplicationRegistrationResponse | (Application & { apiKey?: string }) | null;
  isOpen: boolean;
  onClose: () => void;
}

export const ConnectAppGuideModal: React.FC<ConnectAppGuideModalProps> = ({ appData, isOpen, onClose }) => {
  const [activeStep, setActiveStep] = useState<number>(1);
  const [activeStack, setActiveStack] = useState<'springboot' | 'express' | 'fastapi' | 'curl'>('springboot');
  const [customAppId, setCustomAppId] = useState<string>(appData?.applicationId || 'my-backend-service');
  
  const [copiedSnippet, setCopiedSnippet] = useState(false);
  const [latestEvents, setLatestEvents] = useState<SecurityEvent[]>([]);
  const [isConnected, setIsConnected] = useState<boolean>(false);

  const applicationId = customAppId.trim() || 'my-backend-service';
  const apiKey = (appData as ApplicationRegistrationResponse)?.apiKey || 'api_key_' + applicationId.replace(/-/g, '_');
  const platformUrl = window.location.origin.includes('5173') ? 'http://localhost:8085' : window.location.origin;

  useEffect(() => {
    if (appData?.applicationId) {
      setCustomAppId(appData.applicationId);
    }
  }, [appData]);

  // Poll/Listen for events from this application
  useEffect(() => {
    if (!isOpen || !applicationId) return;

    const checkExistingEvents = async () => {
      try {
        const events = await EventApi.getByApplicationId(applicationId);
        if (events && events.length > 0) {
          setLatestEvents(events);
          setIsConnected(true);
        }
      } catch (e) {
        // ignore
      }
    };

    checkExistingEvents();
    const interval = setInterval(checkExistingEvents, 3000);

    const unsubscribe = securitySocketManager.onAppEvent(applicationId, (newEvent) => {
      setLatestEvents((prev) => [newEvent, ...prev]);
      setIsConnected(true);
    });

    return () => {
      clearInterval(interval);
      unsubscribe();
    };
  }, [isOpen, applicationId]);

  if (!isOpen) return null;

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedSnippet(true);
    setTimeout(() => setCopiedSnippet(false), 2500);
  };

  // Stack Code Snippets
  const springBootPom = `<!-- 1. Add ApiThreadMonitoring Starter to pom.xml -->
<dependency>
    <groupId>com.apisecurity</groupId>
    <artifactId>api-security-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>`;

  const springBootProperties = `# 2. Configure application.properties
api.security.enabled=true
api.security.application-id=${applicationId}
api.security.platform.base-url=${platformUrl}
api.security.platform.api-key=${apiKey}`;

  const expressSnippet = `// Node.js Express Middleware (app.js or server.js)
const axios = require('axios');

app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    axios.post('${platformUrl}/api/v1/events', {
      applicationId: '${applicationId}',
      method: req.method,
      endpoint: req.baseUrl + req.path,
      statusCode: res.statusCode,
      responseTimeMs: Date.now() - start,
      clientIp: req.ip || req.socket.remoteAddress,
      userAgent: req.get('User-Agent')
    }, {
      headers: { 'X-API-Key': '${apiKey}' },
      timeout: 1000
    }).catch(() => {}); // Non-blocking
  });
  next();
});`;

  const fastApiSnippet = `# Python FastAPI Middleware (main.py)
import time, asyncio, httpx
from fastapi import FastAPI, Request

app = FastAPI()

@app.middleware("http")
async def monitor_requests(request: Request, call_next):
    start = time.time()
    response = await call_next(request)
    latency_ms = int((time.time() - start) * 1000)
    
    # Send telemetry asynchronously in background
    payload = {
        "applicationId": "${applicationId}",
        "method": request.method,
        "endpoint": request.url.path,
        "statusCode": response.status_code,
        "responseTimeMs": latency_ms,
        "clientIp": request.client.host if request.client else "127.0.0.1",
        "userAgent": request.headers.get("user-agent", "")
    }
    asyncio.create_task(send_telemetry(payload))
    return response

async def send_telemetry(data):
    try:
        async with httpx.AsyncClient(timeout=1.0) as client:
            await client.post("${platformUrl}/api/v1/events", json=data, headers={"X-API-Key": "${apiKey}"})
    except Exception:
        pass`;

  const curlSnippet = `# Universal cURL Command
curl -X POST "${platformUrl}/api/v1/events" \\
  -H "Content-Type: application/json" \\
  -H "X-API-Key: ${apiKey}" \\
  -d '{
    "applicationId": "${applicationId}",
    "method": "GET",
    "endpoint": "/api/v1/users",
    "statusCode": 200,
    "responseTimeMs": 42,
    "clientIp": "192.168.1.100"
  }'`;

  const steps = [
    { num: 1, title: '1. Target Service' },
    { num: 2, title: '2. Code Integration' },
    { num: 3, title: '3. Verify Connection' },
    { num: 4, title: '4. Live Telemetry' },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/70 backdrop-blur-xs p-4 overflow-y-auto">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-4xl w-full flex flex-col my-8 max-h-[92vh]">
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-200 flex items-center justify-between bg-slate-900 text-white rounded-t-2xl">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-brand-500/20 text-brand-400 rounded-xl border border-brand-500/30">
              <Layers size={22} />
            </div>
            <div>
              <h2 className="text-lg font-bold tracking-tight">Connect Any Backend Application</h2>
              <p className="text-xs text-slate-400 font-mono">
                Capture 100% of incoming & outgoing APIs in real time across any framework
              </p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-white rounded-lg transition-colors">
            <X size={20} />
          </button>
        </div>

        {/* Steps Bar */}
        <div className="px-6 py-3 bg-slate-100/80 border-b border-slate-200 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            {steps.map((step) => {
              const isCurrent = activeStep === step.num;
              const isPassed = activeStep > step.num;
              return (
                <button
                  key={step.num}
                  onClick={() => setActiveStep(step.num)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                    isCurrent
                      ? 'bg-brand-600 text-white shadow-xs'
                      : isPassed
                      ? 'bg-slate-200 text-slate-700 hover:bg-slate-300'
                      : 'text-slate-500 hover:bg-slate-200/60'
                  }`}
                >
                  {isPassed ? <CheckCircle2 size={13} className="text-emerald-600" /> : null}
                  <span>{step.title}</span>
                </button>
              );
            })}
          </div>

          <div className="hidden sm:flex items-center space-x-2 text-xs font-mono">
            <span className="text-slate-500">Target:</span>
            <span className="font-bold text-brand-700 bg-brand-50 px-2 py-0.5 rounded border border-brand-200">
              {applicationId}
            </span>
          </div>
        </div>

        {/* Step Content */}
        <div className="p-6 flex-1 overflow-y-auto space-y-6 text-sm">
          {/* STEP 1: Set Target Service Name */}
          {activeStep === 1 && (
            <div className="space-y-5">
              <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-900 rounded-xl">
                <h4 className="font-bold text-sm">Step 1: Specify Your Application Name / Slug</h4>
                <p className="text-xs text-emerald-800 mt-1">
                  The central monitoring platform auto-registers any new application on its very first telemetry ping! No complex pre-registration required.
                </p>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-mono font-bold text-slate-700 uppercase">Application ID / Slug</label>
                <input
                  type="text"
                  value={customAppId}
                  onChange={(e) => setCustomAppId(e.target.value)}
                  placeholder="e.g. payment-service, billing-api, user-auth"
                  className="w-full px-4 py-2.5 bg-slate-50 border border-slate-300 rounded-xl font-mono text-sm font-bold text-slate-900 focus:outline-none focus:border-brand-500 focus:bg-white"
                />
                <p className="text-[11px] text-slate-500 font-mono">
                  This identifier tags all API requests captured from this service.
                </p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                  <label className="text-[10px] font-mono font-bold text-slate-500 uppercase">Central Platform URL</label>
                  <div className="font-mono text-xs font-bold text-brand-600">{platformUrl}</div>
                </div>

                <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                  <label className="text-[10px] font-mono font-bold text-slate-500 uppercase">Ingestion Endpoint</label>
                  <div className="font-mono text-xs font-bold text-slate-800">{platformUrl}/api/v1/events</div>
                </div>
              </div>

              <div className="flex justify-end pt-2">
                <button
                  onClick={() => setActiveStep(2)}
                  className="px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-semibold text-xs rounded-xl flex items-center space-x-2 shadow-sm"
                >
                  <span>Next: Choose Your Framework & Code</span>
                  <ArrowRight size={16} />
                </button>
              </div>
            </div>
          )}

          {/* STEP 2: Code Integration Across Stacks */}
          {activeStep === 2 && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-sm text-slate-900">Step 2: Add 3-5 Lines of Code to Your Backend</h4>
                  <p className="text-xs text-slate-500">Select your backend technology below for copy-paste instructions:</p>
                </div>
              </div>

              {/* Stack Selector Tabs */}
              <div className="flex items-center space-x-2 border-b border-slate-200 pb-2">
                {[
                  { id: 'springboot', label: 'Spring Boot (Java)' },
                  { id: 'express', label: 'Node.js (Express)' },
                  { id: 'fastapi', label: 'Python (FastAPI / Flask)' },
                  { id: 'curl', label: 'cURL / Shell' },
                ].map((s) => (
                  <button
                    key={s.id}
                    onClick={() => setActiveStack(s.id as any)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                      activeStack === s.id
                        ? 'bg-slate-900 text-white shadow-xs'
                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    {s.label}
                  </button>
                ))}
              </div>

              {/* Active Stack Snippets */}
              {activeStack === 'springboot' && (
                <div className="space-y-3">
                  <div className="relative">
                    <div className="flex items-center justify-between px-4 py-2 bg-slate-800 text-slate-300 text-xs font-mono rounded-t-xl">
                      <span>pom.xml</span>
                      <button
                        onClick={() => copyToClipboard(springBootPom)}
                        className="px-2.5 py-1 bg-slate-700 hover:bg-slate-600 text-white rounded font-semibold text-xs flex items-center space-x-1"
                      >
                        {copiedSnippet ? <Check size={13} /> : <Copy size={13} />}
                        <span>{copiedSnippet ? 'Copied' : 'Copy'}</span>
                      </button>
                    </div>
                    <pre className="p-4 bg-slate-950 text-emerald-400 font-mono text-xs rounded-b-xl overflow-x-auto">
                      {springBootPom}
                    </pre>
                  </div>

                  <div className="relative">
                    <div className="flex items-center justify-between px-4 py-2 bg-slate-800 text-slate-300 text-xs font-mono rounded-t-xl">
                      <span>src/main/resources/application.properties</span>
                      <button
                        onClick={() => copyToClipboard(springBootProperties)}
                        className="px-2.5 py-1 bg-slate-700 hover:bg-slate-600 text-white rounded font-semibold text-xs flex items-center space-x-1"
                      >
                        {copiedSnippet ? <Check size={13} /> : <Copy size={13} />}
                        <span>{copiedSnippet ? 'Copied' : 'Copy'}</span>
                      </button>
                    </div>
                    <pre className="p-4 bg-slate-950 text-amber-300 font-mono text-xs rounded-b-xl overflow-x-auto leading-relaxed">
                      {springBootProperties}
                    </pre>
                  </div>
                </div>
              )}

              {activeStack === 'express' && (
                <div className="relative">
                  <div className="flex items-center justify-between px-4 py-2 bg-slate-800 text-slate-300 text-xs font-mono rounded-t-xl">
                    <span>server.js / app.js</span>
                    <button
                      onClick={() => copyToClipboard(expressSnippet)}
                      className="px-2.5 py-1 bg-slate-700 hover:bg-slate-600 text-white rounded font-semibold text-xs flex items-center space-x-1"
                    >
                      {copiedSnippet ? <Check size={13} /> : <Copy size={13} />}
                      <span>{copiedSnippet ? 'Copied' : 'Copy Code'}</span>
                    </button>
                  </div>
                  <pre className="p-4 bg-slate-950 text-sky-300 font-mono text-xs rounded-b-xl overflow-x-auto leading-relaxed max-h-72">
                    {expressSnippet}
                  </pre>
                </div>
              )}

              {activeStack === 'fastapi' && (
                <div className="relative">
                  <div className="flex items-center justify-between px-4 py-2 bg-slate-800 text-slate-300 text-xs font-mono rounded-t-xl">
                    <span>main.py</span>
                    <button
                      onClick={() => copyToClipboard(fastApiSnippet)}
                      className="px-2.5 py-1 bg-slate-700 hover:bg-slate-600 text-white rounded font-semibold text-xs flex items-center space-x-1"
                    >
                      {copiedSnippet ? <Check size={13} /> : <Copy size={13} />}
                      <span>{copiedSnippet ? 'Copied' : 'Copy Code'}</span>
                    </button>
                  </div>
                  <pre className="p-4 bg-slate-950 text-indigo-300 font-mono text-xs rounded-b-xl overflow-x-auto leading-relaxed max-h-72">
                    {fastApiSnippet}
                  </pre>
                </div>
              )}

              {activeStack === 'curl' && (
                <div className="relative">
                  <div className="flex items-center justify-between px-4 py-2 bg-slate-800 text-slate-300 text-xs font-mono rounded-t-xl">
                    <span>Terminal / Command Prompt</span>
                    <button
                      onClick={() => copyToClipboard(curlSnippet)}
                      className="px-2.5 py-1 bg-slate-700 hover:bg-slate-600 text-white rounded font-semibold text-xs flex items-center space-x-1"
                    >
                      {copiedSnippet ? <Check size={13} /> : <Copy size={13} />}
                      <span>{copiedSnippet ? 'Copied' : 'Copy Command'}</span>
                    </button>
                  </div>
                  <pre className="p-4 bg-slate-950 text-emerald-400 font-mono text-xs rounded-b-xl overflow-x-auto leading-relaxed">
                    {curlSnippet}
                  </pre>
                </div>
              )}

              <div className="flex justify-between pt-2">
                <button onClick={() => setActiveStep(1)} className="btn-secondary">
                  Back
                </button>
                <button onClick={() => setActiveStep(3)} className="btn-primary">
                  <span>Next: Verify Connection</span>
                  <ArrowRight size={14} />
                </button>
              </div>
            </div>
          )}

          {/* STEP 3: Verify Connection */}
          {activeStep === 3 && (
            <div className="space-y-4">
              <div className="p-6 bg-slate-50 border border-slate-200 rounded-xl text-center space-y-3">
                <div className="flex justify-center">
                  {isConnected ? (
                    <div className="p-3 bg-emerald-100 text-emerald-600 rounded-full animate-bounce">
                      <CheckCircle2 size={36} />
                    </div>
                  ) : (
                    <div className="p-3 bg-amber-100 text-amber-600 rounded-full animate-pulse">
                      <Radio size={36} />
                    </div>
                  )}
                </div>

                <h4 className="text-base font-bold text-slate-900">
                  {isConnected ? `Service '${applicationId}' is Connected!` : `Listening for Telemetry from '${applicationId}'...`}
                </h4>

                <p className="text-xs text-slate-600 max-w-md mx-auto">
                  {isConnected
                    ? `Telemetry events are arriving. The framework is actively grabbing API calls and streaming them to the platform.`
                    : `Waiting for the first API call from '${applicationId}'. Launch your application or execute a request to start monitoring.`}
                </p>

                <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full text-xs font-mono font-bold bg-white border border-slate-200 shadow-2xs">
                  <span className={`w-2.5 h-2.5 rounded-full ${isConnected ? 'bg-emerald-500' : 'bg-amber-500 animate-ping'}`} />
                  <span>Pipeline Status: {isConnected ? 'ACTIVE / RECEIVING' : 'WAITING FOR REAL CALLS'}</span>
                </div>
              </div>

              <div className="flex justify-between pt-2">
                <button onClick={() => setActiveStep(2)} className="btn-secondary">
                  Back
                </button>
                <button onClick={() => setActiveStep(4)} className="btn-primary">
                  <span>Next: View Captured Telemetry</span>
                  <ArrowRight size={14} />
                </button>
              </div>
            </div>
          )}

          {/* STEP 4: Live Telemetry */}
          {activeStep === 4 && (
            <div className="space-y-4">
              <div className="p-4 bg-slate-900 text-white rounded-xl flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-sm text-emerald-400 flex items-center space-x-2">
                    <Radio size={16} className="text-emerald-400 animate-pulse" />
                    <span>Real-Time Traffic Captured for '{applicationId}'</span>
                  </h4>
                  <p className="text-xs text-slate-300 mt-0.5">
                    Live stream of endpoints, status codes, latency, and client IP addresses
                  </p>
                </div>
                <span className="px-2.5 py-1 bg-emerald-500/20 text-emerald-300 text-xs font-mono font-bold rounded border border-emerald-500/30">
                  {latestEvents.length} Events Received
                </span>
              </div>

              <div className="border border-slate-200 rounded-xl overflow-hidden bg-white max-h-[260px] overflow-y-auto">
                {latestEvents.length === 0 ? (
                  <div className="p-8 text-center text-xs font-mono text-slate-400">
                    No telemetry events received yet. Hit your application's endpoints to stream live traffic!
                  </div>
                ) : (
                  <table className="w-full text-left border-collapse text-xs font-mono">
                    <thead>
                      <tr className="bg-slate-100 border-b border-slate-200 text-slate-600">
                        <th className="py-2.5 px-3 font-semibold">Method</th>
                        <th className="py-2.5 px-3 font-semibold">Endpoint</th>
                        <th className="py-2.5 px-3 font-semibold">Status</th>
                        <th className="py-2.5 px-3 font-semibold">Latency</th>
                        <th className="py-2.5 px-3 font-semibold">Client IP</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {latestEvents.map((evt) => (
                        <tr key={evt.id || evt.eventId} className="hover:bg-slate-50">
                          <td className="py-2.5 px-3">
                            <span className={`px-1.5 py-0.5 rounded font-bold text-[10px] ${
                              evt.httpMethod === 'POST' ? 'bg-indigo-50 text-indigo-700' :
                              evt.httpMethod === 'GET' ? 'bg-sky-50 text-sky-700' :
                              evt.httpMethod === 'DELETE' ? 'bg-rose-50 text-rose-700' : 'bg-slate-100 text-slate-700'
                            }`}>
                              {evt.httpMethod}
                            </span>
                          </td>
                          <td className="py-2.5 px-3 font-semibold text-slate-900 truncate max-w-[200px]">
                            {evt.endpoint}
                          </td>
                          <td className="py-2.5 px-3">
                            <span className={`px-1.5 py-0.5 rounded font-bold text-[10px] ${
                              evt.statusCode >= 200 && evt.statusCode < 300 ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'
                            }`}>
                              {evt.statusCode}
                            </span>
                          </td>
                          <td className="py-2.5 px-3 text-slate-600">{evt.responseTimeMs} ms</td>
                          <td className="py-2.5 px-3 text-slate-500">{evt.clientIp || '127.0.0.1'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>

              <div className="flex justify-between pt-2">
                <button onClick={() => setActiveStep(3)} className="btn-secondary">
                  Back
                </button>
                <button onClick={onClose} className="btn-primary">
                  Done / Close Guide
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
