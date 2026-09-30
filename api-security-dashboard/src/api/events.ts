import { apiFetch } from './client';
import { SecurityEvent, SecurityEventFilter } from '../types/event';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const EventApi = {
  getEvents: (filter: SecurityEventFilter = {}): Promise<PageResponse<SecurityEvent>> => {
    const params = new URLSearchParams();
    if (filter.applicationId) params.append('applicationId', filter.applicationId);
    if (filter.httpMethod) params.append('httpMethod', filter.httpMethod);
    if (filter.endpoint) params.append('endpoint', filter.endpoint);
    if (filter.statusCode !== undefined) params.append('statusCode', filter.statusCode.toString());
    if (filter.page !== undefined) params.append('page', filter.page.toString());
    if (filter.size !== undefined) params.append('size', filter.size.toString());

    const queryString = params.toString();
    const url = `/api/v1/events${queryString ? `?${queryString}` : ''}`;
    return apiFetch<PageResponse<SecurityEvent>>(url);
  },

  getByApplicationId: async (applicationId: string): Promise<SecurityEvent[]> => {
    const res = await apiFetch<PageResponse<SecurityEvent> | SecurityEvent[]>(`/api/v1/applications/${applicationId}/events`);
    if (Array.isArray(res)) return res;
    if (res && Array.isArray((res as PageResponse<SecurityEvent>).content)) {
      return (res as PageResponse<SecurityEvent>).content;
    }
    return [];
  },
};
