import React, { useEffect, useState, useCallback } from 'react';
import { MainLayout } from '../components/layout/MainLayout';
import { ThreatFilterBar } from '../components/threats/ThreatFilterBar';
import { ThreatTable } from '../components/threats/ThreatTable';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ThreatApi, ThreatPageResponse } from '../api/threats';
import { ThreatFilter } from '../types/threat';
import { ShieldAlert, ChevronLeft, ChevronRight } from 'lucide-react';

export const ThreatsPage: React.FC = () => {
  const [filter, setFilter] = useState<ThreatFilter>({ page: 0, size: 15 });
  const [pageData, setPageData] = useState<ThreatPageResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchThreats = useCallback(async () => {
    try {
      setLoading(true);
      const data = await ThreatApi.getThreats(filter);
      setPageData(data);
    } catch (err) {
      console.error('Failed to fetch threats', err);
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => { fetchThreats(); }, [fetchThreats]);

  const handlePageChange = (newPage: number) => {
    setFilter((prev) => ({ ...prev, page: newPage }));
  };

  return (
    <MainLayout onRefresh={fetchThreats} pageCrumbs={['SOC Console', 'Threats']}>
      {/* Page header */}
      <div className="flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-lg font-semibold text-slate-900">Threat Incidents</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Investigate, filter and acknowledge AI-detected security threats
          </p>
        </div>
        {pageData && (
          <div className="flex items-center gap-2 px-3 py-1.5 bg-red-50 border border-red-200 rounded-md">
            <ShieldAlert size={14} className="text-red-500" />
            <span className="text-sm font-medium text-red-700">{pageData.totalElements} Total Threats</span>
          </div>
        )}
      </div>

      {/* Filter bar */}
      <ThreatFilterBar
        filter={filter}
        onChange={(updated) => setFilter(updated)}
        onReset={() => setFilter({ page: 0, size: 15 })}
      />

      {/* Content */}
      {loading ? (
        <LoadingSpinner message="Fetching threat incidents..." />
      ) : !pageData || pageData.content.length === 0 ? (
        <div className="panel p-12 text-center">
          <ShieldAlert size={32} className="text-slate-300 mx-auto mb-3" />
          <p className="text-sm text-slate-500">No threat incidents match the selected criteria.</p>
        </div>
      ) : (
        <>
          <ThreatTable threats={pageData.content} />

          {/* Pagination */}
          <div className="panel px-4 py-3 flex items-center justify-between shrink-0">
            <p className="text-sm text-slate-500 font-mono">
              Page <strong className="text-slate-800">{pageData.number + 1}</strong> of{' '}
              <strong className="text-slate-800">{pageData.totalPages}</strong>{' '}
              <span className="text-slate-400">({pageData.totalElements} threats)</span>
            </p>
            <div className="flex items-center gap-1">
              <button
                disabled={pageData.number === 0}
                onClick={() => handlePageChange(pageData.number - 1)}
                className="btn-secondary p-1.5 disabled:opacity-40"
              >
                <ChevronLeft size={15} />
              </button>
              <button
                disabled={pageData.number + 1 >= pageData.totalPages}
                onClick={() => handlePageChange(pageData.number + 1)}
                className="btn-secondary p-1.5 disabled:opacity-40"
              >
                <ChevronRight size={15} />
              </button>
            </div>
          </div>
        </>
      )}
    </MainLayout>
  );
};
