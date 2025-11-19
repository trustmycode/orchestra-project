package com.orchestra.api.controller;

import com.orchestra.domain.dto.ScenarioSuiteCreateRequest;
import com.orchestra.domain.dto.ScenarioSuiteDetail;
import com.orchestra.domain.dto.ScenarioSuiteSummary;
import com.orchestra.api.service.ScenarioSuiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scenario-suites")
@RequiredArgsConstructor
public class ScenarioSuiteController {

    private final ScenarioSuiteService scenarioSuiteService;

    @GetMapping
    public ResponseEntity<List<ScenarioSuiteSummary>> getAllSuites() {
        return ResponseEntity.ok(scenarioSuiteService.findAll());
    }

    @PostMapping
    public ResponseEntity<ScenarioSuiteDetail> createSuite(@RequestBody ScenarioSuiteCreateRequest createRequest) {
        ScenarioSuiteDetail created = scenarioSuiteService.create(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScenarioSuiteDetail> getSuiteById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(scenarioSuiteService.findById(id));
    }
}
