package com.orchestra.api.controller;

import com.orchestra.api.service.AiJobService;
import com.orchestra.api.service.AiService;
import com.orchestra.api.service.TestDataSetService;
import com.orchestra.domain.dto.AiJob;
import com.orchestra.domain.dto.TestDataSetDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/jobs")
@RequiredArgsConstructor
public class AiJobController {

    private final AiJobService aiJobService;
    private final AiService aiService;
    private final TestDataSetService testDataSetService;

    @PostMapping("/generate")
    public ResponseEntity<TestDataSetDetail> startGenerationJob(
            @RequestParam(required = false) UUID suiteId,
            @RequestParam(required = false) UUID scenarioId,
            @RequestParam(required = false) UUID environmentId,
            @RequestParam(required = false) String instructions) {
        
        if (suiteId == null && scenarioId == null) {
            return ResponseEntity.badRequest().build();
        }

        AiJob job = aiJobService.createJob();
        
        // Create placeholder dataset immediately
        TestDataSetDetail placeholder = testDataSetService.createPlaceholder(suiteId, scenarioId, job.getId());

        if (suiteId != null) aiService.generateDataForSuiteAsync(suiteId, environmentId, instructions, job.getId(), placeholder.getId());
        else aiService.generateDataForScenarioAsync(scenarioId, environmentId, job.getId(), placeholder.getId());
        
        return ResponseEntity.accepted().body(placeholder);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<AiJob> getJob(@PathVariable UUID jobId) {
        AiJob job = aiJobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }
}

