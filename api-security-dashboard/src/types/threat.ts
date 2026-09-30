export type ThreatSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ThreatStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED';
export type ThreatType = 'CREDENTIAL_STUFFING' | 'BOLA' | 'ID_ENUMERATION' | 'SUSPICIOUS_BEHAVIOR' | 'UNKNOWN';

export interface Threat {
  id: string;
  eventId: string;
  applicationId: string;
  endpoint: string;
  httpMethod: string;
  threatType: ThreatType;
  confidence: number;
  riskScore: number;
  severity: ThreatSeverity;
  status: ThreatStatus;
  recommendedAction?: string;
  reasonCodes: string[];
  detectedAt: string;
  updatedAt?: string;
  clientIp?: string;
  userId?: string;
  description?: string;
}

export interface ThreatFilter {
  applicationId?: string;
  threatType?: string;
  severity?: ThreatSeverity;
  status?: ThreatStatus;
  endpoint?: string;
  page?: number;
  size?: number;
}
