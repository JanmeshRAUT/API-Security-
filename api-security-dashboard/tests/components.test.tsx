import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { SeverityBadge } from '../src/components/common/SeverityBadge';
import { StatusBadge } from '../src/components/common/StatusBadge';
import { RiskGauge } from '../src/components/common/RiskGauge';

describe('Common Components', () => {
  it('renders SeverityBadge correctly', () => {
    render(<SeverityBadge severity="CRITICAL" />);
    expect(screen.getByText('CRITICAL')).toBeInTheDocument();
  });

  it('renders StatusBadge correctly', () => {
    render(<StatusBadge status="ACKNOWLEDGED" />);
    expect(screen.getByText('ACKNOWLEDGED')).toBeInTheDocument();
  });

  it('renders RiskGauge correctly with score', () => {
    render(<RiskGauge score={0.85} label="Risk Score Test" type="risk" />);
    expect(screen.getByText('Risk Score Test')).toBeInTheDocument();
    expect(screen.getByText('0.85 (85%)')).toBeInTheDocument();
  });
});
