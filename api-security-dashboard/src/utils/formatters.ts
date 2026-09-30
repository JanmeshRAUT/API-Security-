import React from 'react';
import { ThreatSeverity, ThreatStatus } from '../types/threat';

export function formatTimestamp(isoString?: string): string {
  if (!isoString) return 'N/A';
  try {
    const date = new Date(isoString);
    return new Intl.DateTimeFormat('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    }).format(date);
  } catch {
    return isoString;
  }
}

export function formatRelativeTime(isoString?: string): string {
  if (!isoString) return 'N/A';
  try {
    const date = new Date(isoString);
    const now = new Date();
    const diffSec = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffSec < 5) return 'Just now';
    if (diffSec < 60) return `${diffSec} seconds ago`;
    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) return `${diffMin} min ago`;
    const diffHr = Math.floor(diffMin / 60);
    if (diffHr < 24) return `${diffHr} hrs ago`;
    const diffDays = Math.floor(diffHr / 24);
    return `${diffDays} days ago`;
  } catch {
    return isoString;
  }
}

export function getSeverityBadgeStyles(severity: ThreatSeverity): string {
  switch (severity) {
    case 'CRITICAL':
      return 'bg-rose-100 text-rose-800 border-rose-300 font-semibold';
    case 'HIGH':
      return 'bg-amber-100 text-amber-900 border-amber-300 font-semibold';
    case 'MEDIUM':
      return 'bg-sky-100 text-sky-800 border-sky-300 font-medium';
    case 'LOW':
    default:
      return 'bg-slate-100 text-slate-700 border-slate-300 font-medium';
  }
}

export function getStatusBadgeStyles(status: ThreatStatus): string {
  switch (status) {
    case 'OPEN':
      return 'bg-rose-50 text-rose-700 border-rose-200';
    case 'ACKNOWLEDGED':
      return 'bg-amber-50 text-amber-700 border-amber-200';
    case 'RESOLVED':
      return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    default:
      return 'bg-slate-50 text-slate-600 border-slate-200';
  }
}

export function getThreatTypeLabel(type: string): string {
  switch (type) {
    case 'CREDENTIAL_STUFFING':
      return 'Credential Stuffing';
    case 'BOLA':
      return 'BOLA / Broken Object Level Auth';
    case 'ID_ENUMERATION':
      return 'ID Enumeration';
    case 'SUSPICIOUS_BEHAVIOR':
      return 'Suspicious Behavior';
    default:
      return type || 'Unknown Threat';
  }
}
