export type EventProcessingStatus = 'PENDING' | 'PROCESSING' | 'PROCESSED' | 'FAILED';

export interface SecurityEvent {
  id: string;
  eventId: string;
  applicationId: string;
  endpoint: string;
  httpMethod: string;
  statusCode: number;
  clientIp: string;
  userAgent?: string;
  userId?: string;
  timestamp: string;
  responseTimeMs: number;
  requestSize?: number;
  responseSize?: number;
  authenticated: boolean;
  processingStatus: EventProcessingStatus;
  createdAt: string;
}

export interface SecurityEventFilter {
  applicationId?: string;
  httpMethod?: string;
  endpoint?: string;
  statusCode?: number;
  page?: number;
  size?: number;
}
