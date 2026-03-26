package com.orchestra.api.service;

import com.orchestra.domain.dto.AiJob;
import com.orchestra.domain.dto.JobEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiJobService {

    private final Map<UUID, AiJob> jobs = new ConcurrentHashMap<>();

    public AiJob createJob() {
        AiJob job = new AiJob();
        job.setId(UUID.randomUUID());
        job.setStatus("QUEUED");
        job.setProgress(0);
        jobs.put(job.getId(), job);
        return job;
    }

    public AiJob getJob(UUID id) {
        return jobs.get(id);
    }

    public synchronized void updateProgress(UUID id, int progress, String message) {
        AiJob job = jobs.get(id);
        if (job != null) {
            job.setStatus("PROCESSING");
            job.setProgress(progress);
            job.setMessage(message);
        }
    }

    public synchronized void addEvent(UUID id, String stage, String description, Object data) {
        AiJob job = jobs.get(id);
        if (job != null) {
            if (job.getEvents() == null) {
                job.setEvents(new ArrayList<>());
            }
            job.getEvents().add(new JobEvent(stage, description, data, LocalDateTime.now()));
        }
    }

    public void complete(UUID id, Object result) {
        AiJob job = jobs.get(id);
        if (job != null) {
            job.setStatus("COMPLETED");
            job.setProgress(100);
            job.setResult(result);
        }
    }

    public void fail(UUID id, String error) {
        AiJob job = jobs.get(id);
        if (job != null) {
            job.setStatus("FAILED");
            job.setError(error);
        }
    }
}

