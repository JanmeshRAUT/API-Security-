import { apiFetch } from './client';
import { Threat, ThreatFilter, ThreatStatus } from '../types/threat';

export interface ThreatPageResponse {
  content: Threat[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const ThreatApi = {
  getThreats: (filter: ThreatFilter = {}): Promise<ThreatPageResponse> => {
    const params = new URLSearchParams();
    if (filter.applicationId) params.append('applicationId', filter.applicationId);
    if (filter.threatType) params.append('threatType', filter.threatType);
    if (filter.severity) params.append('severity', filter.severity);
    if (filter.status) params.append('status', filter.status);
    if (filter.endpoint) params.append('endpoint', filter.endpoint);
    if (filter.page !== undefined) params.append('page', filter.page.toString());
    if (filter.size !== undefined) params.append('size', filter.size.toString());

    const queryString = params.toString();
    const url = `/api/v1/threats${queryString ? `?${queryString}` : ''}`;
    return apiFetch<ThreatPageResponse>(url);
  },

  getById: (id: string): Promise<Threat> => {
    return apiFetch<Threat>(`/api/v1/threats/${id}`);
  },

  updateStatus: (id: string, status: ThreatStatus): Promise<Threat> => {
    return apiFetch<Threat>(`/api/v1/threats/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    });
  },

  getByApplicationId: async (applicationId: string): Promise<Threat[]> => {
    const res = await apiFetch<ThreatPageResponse | Threat[]>(`/api/v1/applications/${applicationId}/threats`);
    if (Array.isArray(res)) return res;
    if (res && Array.isArray((res as ThreatPageResponse).content)) {
      return (res as ThreatPageResponse).content;
    }
    return [];
  },
};
