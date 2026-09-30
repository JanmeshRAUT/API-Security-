import React from 'react';

interface RiskGaugeProps {
  score: number;
  label?: string;
  type?: 'risk' | 'confidence';
}

export const RiskGauge: React.FC<RiskGaugeProps> = ({ score, label, type = 'risk' }) => {
  const percentage = Math.min(Math.max(Math.round(score * 100), 0), 100);

  let barColor = 'bg-emerald-500';
  if (type === 'risk') {
    if (percentage >= 80) barColor = 'bg-rose-600';
    else if (percentage >= 50) barColor = 'bg-amber-500';
    else if (percentage >= 20) barColor = 'bg-sky-500';
  } else {
    // Confidence gauge (higher confidence is blue/indigo)
    if (percentage >= 80) barColor = 'bg-brand-600';
    else if (percentage >= 50) barColor = 'bg-brand-400';
    else barColor = 'bg-slate-400';
  }

  return (
    <div className="w-full">
      <div className="flex justify-between items-center text-xs mb-1 font-mono">
        <span className="text-slate-600 font-medium">{label || (type === 'risk' ? 'Risk Score' : 'Confidence')}</span>
        <span className="font-bold text-slate-800">{score.toFixed(2)} ({percentage}%)</span>
      </div>
      <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden border border-slate-200">
        <div
          className={`h-full ${barColor} transition-all duration-500 ease-out`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
};
