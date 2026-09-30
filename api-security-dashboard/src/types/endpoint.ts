export interface EndpointSummary {
  endpoint: string;
  applicationId: string;
  methods: string[];
  totalRequests: number;
  avgLatencyMs: number;
  minLatencyMs: number;
  maxLatencyMs: number;
  errorCount: number;
  errorRate: number;
  lastSeenAt: string;
}

export interface ApiMetrics {
  totalRequests: number;
  totalEndpoints: number;
  activeApplications: number;
  avgLatencyMs: number;
  errorCount: number;
  errorRate: number;
  statusBreakdown: Record<string, number>;
  methodBreakdown: Record<string, number>;
}
