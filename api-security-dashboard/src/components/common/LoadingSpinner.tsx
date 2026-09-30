import React from 'react';
import { Loader2 } from 'lucide-react';

export const LoadingSpinner: React.FC<{ message?: string }> = ({
  message = 'Loading security data...',
}) => (
  <div className="flex flex-col items-center justify-center py-16 gap-3 text-slate-400">
    <Loader2 className="animate-spin text-blue-600" size={28} />
    <span className="text-sm font-medium text-slate-500">{message}</span>
  </div>
);
