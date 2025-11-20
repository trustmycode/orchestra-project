import React from 'react';
import { Badge } from './ui/badge';
import { cn } from '../lib/utils';

interface Props {
  status: string;
  className?: string;
}

const StatusBadge: React.FC<Props> = ({ status, className }) => {
  const normalizedStatus = status?.toUpperCase() || 'UNKNOWN';

  let variantClass = 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300';

  switch (normalizedStatus) {
    case 'PASSED':
      variantClass =
        'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400 border-emerald-200 dark:border-emerald-800';
      break;
    case 'FAILED':
    case 'FAILED_STUCK':
      variantClass =
        'bg-rose-100 text-rose-700 dark:bg-rose-500/20 dark:text-rose-400 border-rose-200 dark:border-rose-800';
      break;
    case 'RUNNING':
    case 'IN_PROGRESS':
      variantClass =
        'bg-violet-100 text-violet-700 dark:bg-violet-500/20 dark:text-violet-400 border-violet-200 dark:border-violet-800 animate-pulse';
      break;
    case 'PENDING':
    case 'QUEUED':
      variantClass =
        'bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-400 border-amber-200 dark:border-amber-800';
      break;
    case 'SKIPPED':
      variantClass =
        'bg-slate-100 text-slate-500 dark:bg-slate-800 dark:text-slate-500 border-slate-200 dark:border-slate-700';
      break;
    case 'DRAFT':
      variantClass = 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400';
      break;
    case 'PUBLISHED':
      variantClass = 'bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400';
      break;
    default:
      break;
  }

  return (
    <Badge variant="outline" className={cn(variantClass, className)}>
      {normalizedStatus}
    </Badge>
  );
};

export default StatusBadge;
