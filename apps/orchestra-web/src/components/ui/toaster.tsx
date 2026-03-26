import React from 'react';
import { useToast } from './use-toast';
import { X, CheckCircle2, AlertCircle } from 'lucide-react';
import { cn } from '../../lib/utils';

export const Toaster: React.FC = () => {
  const { toasts, dismiss } = useToast();

  return (
    <div className="fixed bottom-0 right-0 z-[100] flex flex-col gap-2 p-4 max-w-[420px] w-full">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={cn(
            "group pointer-events-auto relative flex w-full items-center justify-between space-x-4 overflow-hidden rounded-md border p-6 pr-8 shadow-lg transition-all data-[swipe=cancel]:translate-x-0 data-[swipe=end]:translate-x-[var(--radix-toast-swipe-end-x)] data-[swipe=move]:translate-x-[var(--radix-toast-swipe-move-x)] data-[state=open]:animate-in data-[state=closed]:animate-out data-[swipe=end]:animate-out data-[state=closed]:fade-out-80 data-[state=closed]:slide-out-to-right-full data-[state=open]:slide-in-from-top-full data-[state=open]:sm:slide-in-from-bottom-full",
            "bg-background text-foreground border-border",
            toast.variant === 'destructive' && "destructive group border-destructive bg-destructive text-destructive-foreground",
            toast.className
          )}
        >
          <div className="flex gap-3">
            {toast.variant === 'destructive' ? (
              <AlertCircle className="h-5 w-5 shrink-0" />
            ) : (
              <CheckCircle2 className="h-5 w-5 shrink-0 text-violet-600 dark:text-violet-400" />
            )}
            <div className="grid gap-1">
              {toast.title && <div className="text-sm font-semibold">{toast.title}</div>}
              {toast.description && (
                <div className="text-sm opacity-90">{toast.description}</div>
              )}
            </div>
          </div>
          <button
            onClick={() => dismiss(toast.id)}
            className={cn(
              "absolute right-2 top-2 rounded-md p-1 text-foreground/50 opacity-0 transition-opacity hover:text-foreground focus:opacity-100 focus:outline-none focus:ring-2 group-hover:opacity-100",
              toast.variant === 'destructive' && "text-red-300 hover:text-red-50 focus:ring-red-400 focus:ring-offset-red-600"
            )}
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      ))}
    </div>
  );
};

