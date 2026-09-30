import { apiFetch } from './client';

export interface Settings {
  dataRetentionDays: number;
  incidentRetentionDays: number;
  anomalySensitivity: 'LOW' | 'MEDIUM' | 'HIGH';
  autoBanEnabled: boolean;
  rateLimitSuspicious: boolean;
  alertsEmail: string;
  slackWebhookUrl: string;
  slackAlertsEnabled: boolean;
}

export const SettingsApi = {
  get: (): Promise<Settings> => {
    return apiFetch<Settings>('/api/v1/settings');
  },

  update: (settings: Partial<Settings>): Promise<Settings> => {
    return apiFetch<Settings>('/api/v1/settings', {
      method: 'PUT',
      body: JSON.stringify(settings),
    });
  },
};
