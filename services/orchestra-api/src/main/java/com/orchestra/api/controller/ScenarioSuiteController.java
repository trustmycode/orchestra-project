package com.orchestra.api.controller;

import com.orchestra.domain.dto.ScenarioSuiteCreateRequest;
import com.orchestra.domain.dto.ScenarioSuiteDetail;
import com.orchestra.domain.dto.ScenarioSuiteGenerateRequest;
import com.orchestra.domain.dto.ScenarioSuiteSummary;
import com.orchestra.api.service.ProcessToScenarioGenerator;
import com.orchestra.api.service.ScenarioSuiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scenario-suites")
@RequiredArgsConstructor
public class ScenarioSuiteController {

    private final ScenarioSuiteService scenarioSuiteService;
    private final ProcessToScenarioGenerator processToScenarioGenerator;

    @GetMapping
    public ResponseEntity<List<ScenarioSuiteSummary>> getAllSuites() {
        return ResponseEntity.ok(scenarioSuiteService.findAll());
    }

    @PostMapping
    public ResponseEntity<ScenarioSuiteDetail> createSuite(@RequestBody ScenarioSuiteCreateRequest createRequest) {
        ScenarioSuiteDetail created = scenarioSuiteService.create(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/from-process")
    public ResponseEntity<ScenarioSuiteDetail> generateSuite(@RequestBody ScenarioSuiteGenerateRequest request) {
        ScenarioSuiteDetail generated = processToScenarioGenerator.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(generated);
    }

    @PostMapping("/from-process/async")
    public ResponseEntity<Map<String, UUID>> generateSuiteAsync(@RequestBody ScenarioSuiteGenerateRequest request) {
        Map<String, UUID> result = processToScenarioGenerator.generateAsync(request);
        return ResponseEntity.accepted().body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScenarioSuiteDetail> getSuiteById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(scenarioSuiteService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSuite(@PathVariable("id") UUID id) {
        scenarioSuiteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
