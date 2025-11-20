import React from 'react';
import { Sparkles, X, Check } from 'lucide-react';
import { Button } from '../ui/button';
import { cn } from '../../lib/utils';

interface SuggestionCardProps {
  title: string;
  description: string;
  onApply: () => void;
  onDismiss: () => void;
  className?: string;
}

const SuggestionCard: React.FC<SuggestionCardProps> = ({
  title,
  description,
  onApply,
  onDismiss,
  className,
}) => {
  return (
    <div
      className={cn(
        'relative flex flex-col gap-2 rounded-lg border border-violet-200 bg-violet-50 p-4 dark:border-violet-900 dark:bg-violet-950/20',
        className
      )}
    >
      <div className="flex items-start gap-3">
        <Sparkles className="mt-0.5 h-5 w-5 text-violet-600 dark:text-violet-400" />
        <div className="flex-1">
          <h4 className="text-sm font-semibold text-violet-900 dark:text-violet-100">{title}</h4>
          <p className="mt-1 text-sm text-violet-700 dark:text-violet-300">{description}</p>
        </div>
      </div>
      <div className="mt-2 flex justify-end gap-2">
        <Button variant="ghost" size="sm" onClick={onDismiss} className="h-8 px-2 text-xs">
          <X className="mr-1 h-3 w-3" /> Dismiss
        </Button>
        <Button variant="ai" size="sm" onClick={onApply} className="h-8 px-3 text-xs">
          <Check className="mr-1 h-3 w-3" /> Apply
        </Button>
      </div>
    </div>
  );
};

export default SuggestionCard;
