import { useState, useEffect } from 'react';

export interface Toast {
  id: string;
  title?: string;
  description?: string;
  variant?: 'default' | 'destructive';
  className?: string;
  duration?: number;
}

type ToastAction = Omit<Toast, 'id'>;

let listeners: Array<(toasts: Toast[]) => void> = [];
let memoryToasts: Toast[] = [];

function notify() {
  listeners.forEach((listener) => listener([...memoryToasts]));
}

function addToast(toast: ToastAction) {
  const id = Math.random().toString(36).substring(2, 9);
  const newToast = { ...toast, id };
  memoryToasts = [...memoryToasts, newToast];
  notify();

  if (toast.duration !== Infinity) {
    setTimeout(() => {
      removeToast(id);
    }, toast.duration || 5000);
  }
}

function removeToast(id: string) {
  memoryToasts = memoryToasts.filter((t) => t.id !== id);
  notify();
}

export function useToast() {
  const [toasts, setToasts] = useState<Toast[]>(memoryToasts);

  useEffect(() => {
    listeners.push(setToasts);
    return () => {
      listeners = listeners.filter((l) => l !== setToasts);
    };
  }, []);

  return {
    toasts,
    toast: addToast,
    dismiss: removeToast,
  };
}

