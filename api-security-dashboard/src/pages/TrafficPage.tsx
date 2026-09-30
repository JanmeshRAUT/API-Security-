import React, { useEffect, useState, useCallback } from 'react';
import { MainLayout } from '../components/layout/MainLayout';
import { TrafficFilterBar } from '../components/traffic/TrafficFilterBar';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { EventApi, PageResponse } from '../api/events';
import { SecurityEvent, SecurityEventFilter } from '../types/event';
import { formatTimestamp } from '../utils/formatters';
import { ChevronLeft, ChevronRight, Eye, Activity } from 'lucide-react';
import { RequestInspectorModal } from '../components/traffic/RequestInspectorModal';

const METHOD_COLORS: Record<string, string> = {
  GET:    'bg-sky-50 text-sky-700 border-sky-200',
  POST:   'bg-violet-50 text-violet-700 border-violet-200',
  PUT:    'bg-amber-50 text-amber-700 border-amber-200',
  PATCH:  'bg-orange-50 text-orange-700 border-orange-200',
  DELETE: 'bg-red-50 text-red-700 border-red-200',
};

const statusColor = (code: number) => {
  if (code >= 200 && code < 300) return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (code >= 400) return 'bg-amber-50 text-amber-700 border-amber-200';
  return 'bg-slate-50 text-slate-600 border-slate-200';
};

export const TrafficPage: React.FC = () => {
  const [filter, setFilter] = useState<SecurityEventFilter>({ page: 0, size: 20 });
  const [pageData, setPageData] = useState<PageResponse<SecurityEvent> | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedEvent, setSelectedEvent] = useState<SecurityEvent | null>(null);

  const fetchEvents = useCallback(async () => {
    try {
      setLoading(true);
      const data = await EventApi.getEvents(filter);
      setPageData(data);
    } catch (err) {
      console.error('Failed to fetch security events', err);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => { fetchEvents(); }, [fetchEvents]);

  return (
    <MainLayout onRefresh={fetchEvents} pageCrumbs={['SOC Console', 'Live Traffic']}>
      {/* Page header */}
      <div className="flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-lg font-semibold text-slate-900">API Traffic</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Inspect historical and real-time API events across all registered applications
          </p>
        </div>
        {pageData && (
          <div className="flex items-center gap-2 px-3 py-1.5 bg-blue-50 border border-blue-200 rounded-md">
            <Activity size={14} className="text-blue-500" />
            <span className="text-sm font-medium text-blue-700">{pageData.totalElements.toLocaleString()} Events</span>
          </div>
        )}
      </div>

      {/* Filter bar */}
      <TrafficFilterBar
        filter={filter}
        onChange={(updated) => setFilter(updated)}
        onReset={() => setFilter({ page: 0, size: 20 })}
      />

      {/* Content */}
      {loading ? (
        <LoadingSpinner message="Fetching API traffic logs..." />
      ) : !pageData || pageData.content.length === 0 ? (
        <div className="panel p-12 text-center">
          <Activity size={32} className="text-slate-300 mx-auto mb-3" />
          <p className="text-sm text-slate-500">No API events match the selected filters.</p>
        </div>
      ) : (
        <>
          <div className="panel overflow-hidden">
            <div className="overflow-x-auto">
              <table className="soc-table">
                <thead>
                  <tr>
                    <th>Event ID</th>
                    <th>Timestamp</th>
                    <th>Application</th>
                    <th>Method</th>
                    <th>Endpoint</th>
                    <th>Status</th>
                    <th>Latency</th>
                    <th>Client IP</th>
                    <th>Auth</th>
                    <th className="text-right">Inspect</th>
                  </tr>
                </thead>
                <tbody>
                  {pageData.content.map((evt) => (
                    <tr
                      key={evt.id}
                      onClick={() => setSelectedEvent(evt)}
                      className="cursor-pointer"
                    >
                      <td className="font-mono text-slate-400 max-w-[90px] truncate">{evt.eventId}</td>
                      <td className="font-mono text-slate-500 whitespace-nowrap">{formatTimestamp(evt.timestamp)}</td>
                      <td className="font-mono font-semibold text-slate-900">{evt.applicationId}</td>
                      <td>
                        <span className={`status-pill border ${METHOD_COLORS[evt.httpMethod] ?? 'bg-slate-50 text-slate-600 border-slate-200'}`}>
                          {evt.httpMethod}
                        </span>
                      </td>
                      <td className="font-mono text-slate-700 max-w-[160px] truncate" title={evt.endpoint}>
                        {evt.endpoint}
                      </td>
                      <td>
                        <span className={`status-pill border ${statusColor(evt.statusCode)}`}>
                          {evt.statusCode}
                        </span>
                      </td>
                      <td className="font-mono text-slate-600">{evt.responseTimeMs} ms</td>
                      <td className="font-mono text-slate-500">{evt.clientIp}</td>
                      <td>
                        {evt.authenticated
                          ? <span className="text-emerald-600 font-semibold text-xs">AUTH</span>
                          : <span className="text-slate-400 text-xs">—</span>
                        }
                      </td>
                      <td className="text-right">
                        <button
                          onClick={(e) => { e.stopPropagation(); setSelectedEvent(evt); }}
                          className="inline-flex items-center gap-1 text-xs font-medium text-slate-500 hover:text-blue-600 hover:bg-blue-50 px-2 py-1 rounded transition-colors"
                        >
                          <Eye size={12} />
                          View
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination */}
          <div className="panel px-4 py-3 flex items-center justify-between shrink-0">
            <p className="text-sm text-slate-500 font-mono">
              Page <strong className="text-slate-800">{pageData.number + 1}</strong> of{' '}
              <strong className="text-slate-800">{pageData.totalPages}</strong>{' '}
              <span className="text-slate-400">({pageData.totalElements.toLocaleString()} events)</span>
            </p>
            <div className="flex items-center gap-1">
              <button
                disabled={pageData.number === 0}
                onClick={() => setFilter((p) => ({ ...p, page: pageData.number - 1 }))}
                className="btn-secondary p-1.5 disabled:opacity-40"
              >
                <ChevronLeft size={15} />
              </button>
              <button
                disabled={pageData.number + 1 >= pageData.totalPages}
                onClick={() => setFilter((p) => ({ ...p, page: pageData.number + 1 }))}
                className="btn-secondary p-1.5 disabled:opacity-40"
              >
                <ChevronRight size={15} />
              </button>
            </div>
          </div>
        </>
      )}

      <RequestInspectorModal
        event={selectedEvent}
        isOpen={!!selectedEvent}
        onClose={() => setSelectedEvent(null)}
      />
    </MainLayout>
  );
};
