package com.orchestra.domain.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AiJob {
    private UUID id;
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED
    private Integer progress;
    private String message;
    private Object result;
    private String error;
    private List<JobEvent> events = new ArrayList<>();
}

