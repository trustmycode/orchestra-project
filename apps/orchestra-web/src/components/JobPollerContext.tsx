import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { getAiJob } from '../api';
import { useToast } from './ui/use-toast';

interface Job {
  id: string;
  description: string;
}

interface JobPollerContextType {
  trackJob: (jobId: string, description: string) => void;
}

const STORAGE_KEY = 'orchestra_active_jobs';

const JobPollerContext = createContext<JobPollerContextType | undefined>(undefined);

export const JobPollerProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [jobs, setJobs] = useState<Job[]>(() => {
    if (typeof window !== 'undefined') {
      try {
        const stored = localStorage.getItem(STORAGE_KEY);
        return stored ? JSON.parse(stored) : [];
      } catch (e) {
        console.error('Failed to parse stored jobs', e);
        return [];
      }
    }
    return [];
  });
  const { toast } = useToast();

  // Persist jobs to LocalStorage whenever they change
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(jobs));
  }, [jobs]);

  const trackJob = useCallback((jobId: string, description: string) => {
    setJobs((prev) => {
      if (prev.some((j) => j.id === jobId)) return prev;
      return [...prev, { id: jobId, description }];
    });
    toast({
      title: 'Job Started',
      description: `${description} has been queued.`,
    });
  }, [toast]);

  useEffect(() => {
    if (jobs.length === 0) return;

    const intervalId = setInterval(async () => {
      const activeJobs: Job[] = [];

      for (const job of jobs) {
        try {
          const jobStatus = await getAiJob(job.id);

          if (jobStatus.status === 'COMPLETED') {
            toast({
              title: 'Job Completed',
              description: `${job.description} finished successfully.`,
              variant: 'default', // Using default/success style
              className: 'border-emerald-500 bg-emerald-50 dark:bg-emerald-900/20',
            });
          } else if (jobStatus.status === 'FAILED') {
            toast({
              title: 'Job Failed',
              description: `${job.description} failed: ${jobStatus.error || 'Unknown error'}`,
              variant: 'destructive',
            });
          } else {
            // Still running
            activeJobs.push(job);
          }
        } catch (error) {
          console.error(`Failed to poll job ${job.id}`, error);
          // Keep tracking in case it's a transient network error, 
          // but in a real app we might want a retry limit.
          activeJobs.push(job);
        }
      }

      // Update state only if list changed
      if (activeJobs.length !== jobs.length) {
        setJobs(activeJobs);
      }
    }, 3000);

    return () => clearInterval(intervalId);
  }, [jobs, toast]);

  return (
    <JobPollerContext.Provider value={{ trackJob }}>
      {children}
    </JobPollerContext.Provider>
  );
};

export const useJobPoller = () => {
  const context = useContext(JobPollerContext);
  if (!context) {
    throw new Error('useJobPoller must be used within a JobPollerProvider');
  }
  return context;
};

