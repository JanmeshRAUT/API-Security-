import { ThreatSeverity } from './threat';

export interface DashboardSummary {
  totalApplications: number;
  totalEvents: number;
  totalThreats: number;
  criticalThreats: number;
  highThreats: number;
  threatsByType: Record<string, number>;
  threatsBySeverity: Record<ThreatSeverity, number>;
}
