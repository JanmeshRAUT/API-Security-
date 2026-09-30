export type ApplicationStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export interface Application {
  id: string;
  name: string;
  applicationId: string;
  description?: string;
  status: ApplicationStatus;
  createdAt: string;
  lastSeenAt?: string;
  threatCount?: number;
}

export interface ApplicationRegistrationRequest {
  name: string;
  applicationId: string;
  description?: string;
}

export interface ApplicationRegistrationResponse {
  id: string;
  name: string;
  applicationId: string;
  apiKey: string;
  description?: string;
  status: ApplicationStatus;
  createdAt: string;
}
