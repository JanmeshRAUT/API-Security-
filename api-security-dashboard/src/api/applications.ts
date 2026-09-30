import { apiFetch } from './client';
import { Application, ApplicationRegistrationRequest, ApplicationRegistrationResponse } from '../types/application';

export const ApplicationApi = {
  getAll: (): Promise<Application[]> => {
    return apiFetch<Application[]>('/api/v1/applications');
  },

  getById: (id: string): Promise<Application> => {
    return apiFetch<Application>(`/api/v1/applications/${id}`);
  },

  register: (request: ApplicationRegistrationRequest): Promise<ApplicationRegistrationResponse> => {
    return apiFetch<ApplicationRegistrationResponse>('/api/v1/applications', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },

  delete: (id: string): Promise<void> => {
    return apiFetch<void>(`/api/v1/applications/${id}`, {
      method: 'DELETE',
    });
  },

  rotateKey: (id: string): Promise<ApplicationRegistrationResponse> => {
    return apiFetch<ApplicationRegistrationResponse>(`/api/v1/applications/${id}/rotate-key`, {
      method: 'POST',
    });
  },
};
