import { apiFetch } from './client';
import { DashboardSummary } from '../types/dashboard';

export const DashboardApi = {
  getSummary: (): Promise<DashboardSummary> => {
    return apiFetch<DashboardSummary>('/api/v1/dashboard/summary');
  },
};
