import React from 'react';
import { useSecurityContext } from '../../context/SecurityContext';

export const ConnectionBadge: React.FC = () => {
  const { connectionState } = useSecurityContext();

  const isConnected = connectionState === 'CONNECTED';
  const isConnecting = connectionState === 'CONNECTING';

  return (
    <div className={`flex items-center gap-1.5 px-2 py-1 rounded-md border text-[11px] font-semibold font-mono ${
      isConnected
        ? 'bg-emerald-50 border-emerald-200 text-emerald-700'
        : isConnecting
        ? 'bg-amber-50 border-amber-200 text-amber-700'
        : 'bg-red-50 border-red-200 text-red-700'
    }`}>
      <span className={`w-1.5 h-1.5 rounded-full shrink-0 ${
        isConnected ? 'bg-emerald-500 animate-pulse' :
        isConnecting ? 'bg-amber-400 animate-pulse' :
        'bg-red-500'
      }`} />
      <span>{isConnected ? 'LIVE' : isConnecting ? 'CONNECTING' : 'OFFLINE'}</span>
    </div>
  );
};
