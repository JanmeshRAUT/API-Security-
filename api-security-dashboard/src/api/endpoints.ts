import { apiFetch } from './client';
import { EndpointSummary, ApiMetrics } from '../types/endpoint';

export async function fetchEndpoints(applicationId?: string): Promise<EndpointSummary[]> {
  const query = applicationId ? `?applicationId=${encodeURIComponent(applicationId)}` : '';
  return apiFetch<EndpointSummary[]>(`/api/v1/endpoints${query}`);
}

export async function fetchApiMetrics(applicationId?: string): Promise<ApiMetrics> {
  const query = applicationId ? `?applicationId=${encodeURIComponent(applicationId)}` : '';
  return apiFetch<ApiMetrics>(`/api/v1/endpoints/metrics${query}`);
}
