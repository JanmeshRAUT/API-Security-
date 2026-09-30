import React from 'react';
import { ThreatStatus } from '../../types/threat';

interface StatusBadgeProps {
  status: ThreatStatus | string;
  size?: 'sm' | 'md';
}

const styles: Record<string, string> = {
  OPEN:         'status-critical',
  ACKNOWLEDGED: 'status-medium',
  RESOLVED:     'status-safe',
  ACTIVE:       'status-safe',
  INACTIVE:     'status-neutral',
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => (
  <span className={`status-pill ${styles[status] ?? 'status-neutral'}`}>
    {status}
  </span>
);
