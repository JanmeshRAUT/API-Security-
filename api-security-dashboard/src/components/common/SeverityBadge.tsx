import React from 'react';
import { ThreatSeverity } from '../../types/threat';

interface SeverityBadgeProps {
  severity: ThreatSeverity;
  size?: 'sm' | 'md' | 'lg';
}

const styles: Record<ThreatSeverity, string> = {
  CRITICAL: 'status-critical',
  HIGH:     'status-high',
  MEDIUM:   'status-medium',
  LOW:      'status-low',
};

export const SeverityBadge: React.FC<SeverityBadgeProps> = ({ severity }) => (
  <span className={`status-pill ${styles[severity] ?? 'status-neutral'}`}>
    <span className="inline-block w-1.5 h-1.5 rounded-full bg-current shrink-0" />
    {severity}
  </span>
);
