import React from 'react';
import { X, Copy, Check, Clock, Globe, ArrowUpRight, ArrowDownLeft, Shield, User, Laptop } from 'lucide-react';
import { SecurityEvent } from '../../types/event';
import { formatTimestamp } from '../../utils/formatters';

interface RequestInspectorModalProps {
  event: SecurityEvent | null;
  isOpen: boolean;
  onClose: () => void;
}

export const RequestInspectorModal: React.FC<RequestInspectorModalProps> = ({ event, isOpen, onClose }) => {
  const [copied, setCopied] = React.useState(false);

  if (!isOpen || !event) return null;

  const copyJson = () => {
    navigator.clipboard.writeText(JSON.stringify(event, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const getStatusBadge = (status: number) => {
    if (status >= 200 && status < 300) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    if (status >= 300 && status < 400) return 'bg-blue-50 text-blue-700 border-blue-200';
    if (status >= 400 && status < 500) return 'bg-amber-50 text-amber-700 border-amber-200';
    return 'bg-rose-50 text-rose-700 border-rose-200';
  };

  const getMethodBadge = (method: string) => {
    switch (method.toUpperCase()) {
      case 'GET': return 'bg-sky-50 text-sky-700 border-sky-200';
      case 'POST': return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'PUT': return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'DELETE': return 'bg-rose-50 text-rose-700 border-rose-200';
      default: return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
      <div className="panel max-w-3xl w-full flex flex-col max-h-[90vh] overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-900 text-white shrink-0 rounded-t-xl">
          <div className="flex items-center gap-3">
            <span className={`px-2.5 py-1 rounded-md text-xs font-bold font-mono border ${getMethodBadge(event.httpMethod)}`}>
              {event.httpMethod}
            </span>
            <span className="font-mono text-sm font-semibold truncate max-w-md">{event.endpoint}</span>
            <span className={`px-2 py-0.5 rounded-full text-xs font-bold border ${getStatusBadge(event.statusCode)}`}>
              {event.statusCode}
            </span>
          </div>
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-white rounded-lg transition-colors">
            <X size={18} />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-6 text-sm">
          {/* Key Metrics Row */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl">
              <span className="text-[10px] font-mono text-slate-500 uppercase flex items-center gap-1">
                <Clock size={12} />
                <span>Latency</span>
              </span>
              <p className="text-base font-bold font-mono text-slate-900 mt-1">{event.responseTimeMs} ms</p>
            </div>

            <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl">
              <span className="text-[10px] font-mono text-slate-500 uppercase flex items-center gap-1">
                <Globe size={12} />
                <span>Client IP</span>
              </span>
              <p className="text-base font-bold font-mono text-slate-900 mt-1">{event.clientIp || '127.0.0.1'}</p>
            </div>

            <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl">
              <span className="text-[10px] font-mono text-slate-500 uppercase flex items-center gap-1">
                <ArrowUpRight size={12} />
                <span>Req Size</span>
              </span>
              <p className="text-base font-bold font-mono text-slate-900 mt-1">
                {event.requestSize ? `${event.requestSize} B` : '0 B'}
              </p>
            </div>

            <div className="p-3 bg-slate-50 border border-slate-200 rounded-xl">
              <span className="text-[10px] font-mono text-slate-500 uppercase flex items-center gap-1">
                <ArrowDownLeft size={12} />
                <span>Res Size</span>
              </span>
              <p className="text-base font-bold font-mono text-slate-900 mt-1">
                {event.responseSize ? `${event.responseSize} B` : '0 B'}
              </p>
            </div>
          </div>

          {/* Details Table */}
          <div className="border border-slate-200 rounded-xl overflow-hidden bg-white">
            <table className="w-full text-left border-collapse text-xs">
              <tbody className="divide-y divide-slate-100">
                <tr>
                  <td className="py-2.5 px-4 bg-slate-50 font-semibold text-slate-600 w-1/4 border-r border-slate-100">Event ID</td>
                  <td className="py-2.5 px-4 font-mono text-slate-700">{event.eventId}</td>
                </tr>
                <tr>
                  <td className="py-2.5 px-4 bg-slate-50 font-semibold text-slate-600 border-r border-slate-100">Timestamp</td>
                  <td className="py-2.5 px-4 font-mono text-slate-700">{formatTimestamp(event.timestamp)}</td>
                </tr>
                <tr>
                  <td className="py-2.5 px-4 bg-slate-50 font-semibold text-slate-600 border-r border-slate-100">Application</td>
                  <td className="py-2.5 px-4 font-bold text-slate-900 font-mono">{event.applicationId}</td>
                </tr>
                <tr>
                  <td className="py-2.5 px-4 bg-slate-50 font-semibold text-slate-600 border-r border-slate-100 flex items-center gap-2">
                    <User size={13} /> User Agent
                  </td>
                  <td className="py-2.5 px-4 text-slate-600 font-mono break-all">{event.userAgent || 'Unknown'}</td>
                </tr>
                <tr>
                  <td className="py-2.5 px-4 bg-slate-50 font-semibold text-slate-600 border-r border-slate-100 flex items-center gap-2">
                    <Shield size={13} /> Auth Status
                  </td>
                  <td className="py-2.5 px-4">
                    {event.authenticated ? (
                      <span className="inline-flex items-center gap-1 text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200 font-semibold">
                        <Check size={12} /> Authenticated
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 text-slate-500 bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
                        Anonymous
                      </span>
                    )}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <div className="pt-2">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-bold font-mono text-slate-500 uppercase tracking-widest">Raw Payload (JSON)</span>
              <button 
                onClick={copyJson}
                className="btn-secondary text-[11px] py-1 px-2"
              >
                {copied ? <Check size={12} className="text-emerald-500" /> : <Copy size={12} />}
                <span>{copied ? 'Copied' : 'Copy'}</span>
              </button>
            </div>
            <pre className="p-4 bg-slate-900 text-sky-300 font-mono text-xs rounded-xl overflow-x-auto">
              {JSON.stringify(event, null, 2)}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};
