package com.orchestra.api.controller;

import com.orchestra.api.service.SuiteRunService;
import com.orchestra.domain.dto.SuiteRunCreateRequest;
import com.orchestra.domain.dto.SuiteRunDetail;
import com.orchestra.domain.dto.SuiteRunSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suite-runs")
@RequiredArgsConstructor
public class SuiteRunController {

    private final SuiteRunService suiteRunService;

    @PostMapping
    public ResponseEntity<SuiteRunSummary> createSuiteRun(@RequestBody SuiteRunCreateRequest request) {
        // Возвращаем 202 Accepted, так как запуск асинхронный (через Scheduler)
        SuiteRunSummary summary = suiteRunService.createAndSchedule(request);
        return ResponseEntity.accepted().body(summary);
    }

    @GetMapping
    public ResponseEntity<List<SuiteRunSummary>> getAllSuiteRuns() {
        return ResponseEntity.ok(suiteRunService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuiteRunDetail> getSuiteRunById(@PathVariable UUID id) {
        return ResponseEntity.ok(suiteRunService.findById(id));
    }
}

